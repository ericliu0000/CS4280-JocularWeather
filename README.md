# CS4280-JocularWeather

_Eric Liu, Ganning Xu_

## Data Storage

We created an AWS Lambda function that can receive a zip code and store it in a Supabase table. This serverless architecture allows us to also extract the IP address and location of the incoming request.

This serverless function was written in `node.js` using the Serverless framework, which makes deploying to AWS easier.

Documentation:
`GET` `https://98q0kalf91.execute-api.us-east-1.amazonaws.com/pushdb?zip=<zip>&lon=<longitude>&lat=<latitude>`

- Making a `GET` request to this URL will push the zipcode, longitude, and latitude, all to Supabase.

`GET` `https://98q0kalf91.execute-api.us-east-1.amazonaws.com/ip`

- Making a `GET` request to this URL will return the city that's associated with the source IP address

The AWS (for `/pushdb`) code can be found below:

```js
const { createClient } = require("@supabase/supabase-js");
const fetch = require("node-fetch");
require("dotenv").config();

// Get Supabase URL and API key from environment variables
const supabaseUrl = process.env.PROJECT_URL;
const supabaseKey = process.env.SUPABASE_KEY;

// Create Supabase client instance
const supabase = createClient(supabaseUrl, supabaseKey);

// Define AWS Lambda handler function
module.exports.handler = async (event) => {
  // Retrieve zip code, longitude, and latitude from query parameters
  const zipCode = event.queryStringParameters.zip;
  const lon = event.queryStringParameters.lon;
  const lat = event.queryStringParameters.lat;

  // Check if zip code is valid
  if (zipCode === undefined || zipCode === null || zipCode === "") {
    return {
      statusCode: 400,
      body: JSON.stringify({
        error: "Invalid zip code",
      }),
    };
  }

  let sourceIp;
  let userAgent;
  let loc = null;

  try {
    // Retrieve user's IP address and location information using an external API
    sourceIp = event.requestContext?.http.sourceIp;
    loc = await getLocFromIP(sourceIp);
    userAgent = event.requestContext?.http.userAgent;
  } catch (error) {
    // Return error response if location information cannot be retrieved
    return {
      statusCode: 500,
      body: JSON.stringify({
        error: "Error getting location",
      }),
    };
  }

  // Insert new record into Supabase database table
  const { data, error } = await supabase.from("zips").insert({
    zip: zipCode,
    sourceIp: sourceIp,
    userAgent: userAgent,
    country: loc?.country,
    city: loc?.city,
    regionName: loc?.regionName,
    lon: lon,
    lat: lat,
  });

  // Log any errors that occur during the database insert operation
  if (error) {
    console.log(error);
  }

  // Return success response with inserted record data and original event information
  return {
    statusCode: 200,
    body: JSON.stringify({
      data: data,
      event: event,
    }),
  };
};

// Helper function to retrieve location information from IP address
async function getLocFromIP(ip) {
  const ENDPOINT = `http://ip-api.com/json/${ip}`;

  const resp = await fetch(ENDPOINT);
  const data = await resp.json();

  const { country, city, regionName } = data;

  return { country, city, regionName };
}
```

The AWS (for `/ip`) code can be found below:

```js
const fetch = require("node-fetch");

module.exports.handler = async (event) => {
  const ip = event.requestContext?.http.sourceIp || "204.85.24.5";
  const city = await getLocFromIP(ip);
  console.log(city);
  return {
    statusCode: 200,
    body: city.city,
  };
};

async function getLocFromIP(ip) {
  const ENDPOINT = `http://ip-api.com/json/${ip}`;

  const resp = await fetch(ENDPOINT);
  const data = await resp.json();

  const { country, city, regionName } = data;

  return { country, city, regionName };
}
```
