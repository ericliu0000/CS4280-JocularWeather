# CS4280-JocularWeather

## Data Storage

We created an AWS Lambda function that can receive a zip code and store it in a Supabase table. This serverless architecture allows us to also extract the IP address and location of the incoming request.

This serverless function was written in `node.js` using the Serverless framework, which makes deploying to AWS easier.

Documentation:
`POST` `https://98q0kalf91.execute-api.us-east-1.amazonaws.com?zip=five_digit_zip_code>`
