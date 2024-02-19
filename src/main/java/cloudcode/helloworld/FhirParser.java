/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloudcode.helloworld;

import java.io.IOException;
import java.util.Collections;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.healthcare.v1.CloudHealthcareScopes;
import com.google.auth.oauth2.GoogleCredentials;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.util.BundleBuilder;

public class FhirParser {

  private static final Logger logger = LoggerFactory.getLogger(FhirParser.class);

  private static final String PROJECT_ID = "hader-poc-001";
  private static final String DATASET_LOCATION = "us-central1";
  private static final String DATASET_ID = "test-dataset";
  private static final String FHIR_STORE_ID = "test-fhir-store";
  private static final String FHIR_STORE_NAME = String.format(
      "https://healthcare.googleapis.com/v1beta1/projects/%s/locations/%s/datasets/%s/fhirStores/%s",
      PROJECT_ID, DATASET_LOCATION, DATASET_ID, FHIR_STORE_ID);

  public String parseFhirResource(String requestBody) throws Exception {

    
    FhirContext ctx = FhirContext.forR4();

    Resource resourceToCreate = parseFHIREntity(ctx, requestBody);

    String serializedResource = ctx.newJsonParser().encodeResourceToString(buildBundle(ctx, resourceToCreate));

    createFHIREntity(ctx, resourceToCreate);

    return serializedResource;

  }

  private void createFHIREntity(FhirContext ctx, Resource resource) throws IOException {
    try {

      String fhirStoreUrl = String.format("%s/fhir", FHIR_STORE_NAME);

      String token = getAccessToken();

      BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(token);

      IGenericClient HAPIFhirClient = ctx.newRestfulGenericClient(fhirStoreUrl);
      HAPIFhirClient.registerInterceptor(authInterceptor);

      // Use the client to store a new resource instance
      MethodOutcome outcome = HAPIFhirClient
          .create()
          .resource(resource)
          .execute();
      IIdType id = outcome.getId();
       logger.info("Got ID: " + id.getValue());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getAccessToken() throws IOException {
    GoogleCredentials credential = GoogleCredentials.getApplicationDefault()
        .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    return credential.refreshAccessToken().getTokenValue();
  }

  /**
   * Parses the FHIR resource from the request body.
   *
   * @param ctx         The FHIR context.
   * @param requestBody The request body containing the FHIR resource.
   * @return The parsed FHIR resource.
   * @throws IOException if the request fails.
   */
  private Resource parseFHIREntity(FhirContext ctx, String requestBody) throws IOException {
    // Parse it
    Resource resourceParsed = (Resource) ctx.newJsonParser().parseResource(requestBody);
    if (resourceParsed instanceof Patient) {
      return processPatient((Patient) resourceParsed);
    } else if (resourceParsed instanceof Observation) {
      Observation observation = (Observation) resourceParsed;
      System.out.println("Observation value: " + observation.getValueQuantity().getValue());
    } else {
      System.out.println("Unknown resource type");
    }
    return null;
  }

  /**
   * Processes the Patient resource.
   *
   * @param patient The Patient resource.
   * @return The processed Patient resource.
   */
  private Resource processPatient(Patient patient) {
    // Execute logic on Patient
    patient.setActive(true);
    patient.addIdentifier().setSystem("http://demo-fhir").setValue("demo-test");
    return patient;
  }

  /**
   * Builds a Bundle object that contains the FHIR resource.
   *
   * @param ctx      The FHIR context.
   * @param resource The FHIR resource to create.
   * @return The Bundle object.
   */
  private IBaseBundle buildBundle(FhirContext ctx, Resource resource) {
    BundleBuilder builder = new BundleBuilder(ctx);
    // Add the patient as a create (aka POST) to the Bundle
    builder.addTransactionCreateEntry(resource);
    return builder.getBundle();
  }

}