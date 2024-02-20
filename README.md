# SimpleFhirProxy

The SimpleFhirProxy is a simple FHIR proxy that allows users to create FHIR resources in a Cloud Healthcare API FHIR store.

## Prerequisites

To use the SimpleFhirProxy, you will need the following:

* A Google Cloud Platform project
* A Cloud Healthcare API FHIR store
* The [Google Cloud CLI](/sdk/docs)
* [Cloud Run](/run/docs)
* [curl](https://curl.haxx.se/)

## Installation

To install the SimpleFhirProxy, follow these steps:

1. Clone the SimpleFhirProxy repository from GitHub:
    
    ```
    git clone https://github.com/haderceron/simple-fhir-proxy.git
    ```

2. Change to the directory that contains the SimpleFhirProxy source code:
    
    ```
    cd simple-fhir-proxy
    ```

3. Install the dependencies:
    
    ```
    mvn clean install
    ```

## Deployment

To deploy the SimpleFhirProxy to Cloud Run, follow these steps:

1. Create a Cloud Run service:
    
    ```
    gcloud run deploy simple-fhir-proxy \
        --image gcr.io/PROJECT_ID/simple-fhir-proxy \
        --platform managed \
        --region REGION \
        --allow-unauthenticated
    ```
    
    Replace the following:
    
      - `  PROJECT_ID  ` : your Google Cloud project ID
      - `  REGION  ` : the region where you want to deploy your Cloud Run service

2. Get the URL of your Cloud Run service:
    
    ```
    gcloud run services describe simple-fhir-proxy \
        --platform managed \
        --region REGION
    ```
    
    The output of this command will include the URL of your Cloud Run service.

## Usage

To use the SimpleFhirProxy with `  curl  ` , follow these steps:

1. Open a terminal window.

2. Create a JSON file that contains the FHIR resource that you want to create. For example, the following JSON file creates a Patient resource:
    
    ```
    {
      "resourceType": "Patient",
      "id": "1234567890",
      "meta": {
        "lastUpdated": "2022-01-01T00:00:00+00:00"
      },
      "name": [
        {
          "use": "official",
          "family": "Simpson",
          "given": [
            "Homer"
          ]
        }
      ],
      "gender": "male",
      "birthDate": "1955-05-01"
    }
    ```

4. Use `  curl  ` to send a POST request to the Cloud Run service with the JSON file as the request body. For example, the following command sends a POST request to the Cloud Run service with the JSON file created in the previous step:
    
    ```
    curl -i -m 70 -X POST CLOUD_RUN_SERVICE_URL/fhir-proxy \
        -H "Content-Type: application/json" \
        -d @patient.json
    ```
    Replace the following:
    
      - `  CLOUD_RUN_SERVICE_URL  ` : your Cloud Run Service URL 

    The output of this command will include the response from the Cloud Run service. If the request is successful, the response will contain a message that FHIR resource was created.

## License

The SimpleFhirProxy is licensed under the Apache License, Version 2.0.
