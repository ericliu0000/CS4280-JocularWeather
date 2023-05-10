# CS4280-JocularWeather

## Data Storage

We created an AWS Lambda function that can receive a zip code and store it in a Supabase table. This serverless architecture allows us to also extract the IP address and location of the incoming request.

This serverless function was written in `node.js` using the Serverless framework, which makes deploying to AWS easier.

Documentation:
`POST` `<https://98q0kalf91.execute-api.us-east-1.amazonaws.com?zip=five_digit_zip_code>`

The Lambda function code can be found below:
```js
const { createClient } = require("@supabase/supabase-js");
const fetch = require("node-fetch");
require("dotenv").config();

const supabaseUrl = process.env.SUPABSE_URL;
const supabaseKey = process.env.SUPABASE_KEY;
const supabase = createClient(supabaseUrl, supabaseKey);

module.exports.handler = async (event) => {
  const zipCode = event.queryStringParameters.zip;

  if (
    zipCode === undefined ||
    zipCode === null ||
    zipCode === "" ||
    zipCode.length !== 5
  ) {
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
    sourceIp = event.requestContext?.http.sourceIp;
    loc = await getLocFromIP(sourceIp);
    userAgent = event.requestContext?.http.userAgent;
  } catch (error) {
    return {
      statusCode: 500,
      body: JSON.stringify({
        error: "Error getting location",
      }),
    };
  }

  const { data, error } = await supabase.from("zips").insert({
    zip: zipCode,
    sourceIp: sourceIp,
    userAgent: userAgent,
    country: loc?.country,
    city: loc?.city,
    regionName: loc?.regionName,
  });

  if (error) {
    console.log(error);
  }

  return {
    statusCode: 200,
    body: JSON.stringify({
      data: data,
      event: event,
    }),
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