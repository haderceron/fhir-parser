package cloudcode.simplefhirproxy;

import java.io.IOException;
import java.util.Collections;

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

/**
 * A simple FHIR proxy that allows users to create FHIR resources in a Cloud
 * Healthcare API FHIR
 * store.
 *
 * @author haderceron
 */
public final class SimpleFhirProxy {

  private static final Logger logger = LoggerFactory.getLogger(SimpleFhirProxy.class);

  /**
   * The project ID of the project that contains the FHIR store.
   */
  private static final String PROJECT_ID = System.getenv("PROJECT_ID");

  /**
   * The location of the dataset that contains the FHIR store.
   */
  private static final String DATASET_LOCATION = System.getenv("DATASET_LOCATION");

  /**
   * The ID of the dataset that contains the FHIR store.
   */
  private static final String DATASET_ID = System.getenv("DATASET_ID");

  /**
   * The ID of the FHIR store to which resources will be written.
   */
  private static final String FHIR_STORE_ID = System.getenv("FHIR_STORE_ID");

  /**
   * The URL of the FHIR store.
   */
  private static final String FHIR_STORE_NAME = String.format(
      "https://healthcare.googleapis.com/v1beta1/projects/%s/locations/%s/datasets/%s/fhirStores/%s",
      PROJECT_ID, DATASET_LOCATION, DATASET_ID, FHIR_STORE_ID);

  /**
   * Creates a new instance of the SimpleFhirProxy class.
   */
  public SimpleFhirProxy() {
  }

  /**
   * Preprocesses the FHIR resource to be created.
   *
   * @param ctx         the FHIR context
   * @param requestBody the FHIR resource in JSON format
   * @return the preprocessed FHIR resource
   */
  public Resource preprocessFhirResource(FhirContext ctx, String requestBody) {

    // Parse the FHIR resource from the request body.
    Resource resource = (Resource) ctx.newJsonParser().parseResource(requestBody);

    // If the resource is a Patient, add an identifier to it.
    if (resource instanceof Patient) {
      Patient patient = (Patient) resource;
      patient.addIdentifier().setSystem("http://simple-fhi-proxy").setValue("simple-fhi-proxy");
      resource = patient;
    } // If the resource is an Observation, add an identifier to it.
    else if (resource instanceof Observation) {
      Observation observation = (Observation) resource;
      observation.addIdentifier().setSystem("http://simple-fhi-proxy").setValue("simple-fhi-proxy");
      resource = observation;
    } // If the resource is neither a Patient nor an Observation, log an error and
      // return null.
    else {
      logger.error("Unknown resource type");
      resource = null;
    }

    // Return the preprocessed FHIR resource.
    return resource;

  }

  /**
   * Creates a new FHIR entity in the FHIR store.
   *
   * @param ctx              the FHIR context
   * @param resourceToCreate the FHIR resource to be created
   * @return true if the resource was created successfully, false otherwise
   */
  public boolean createFHIREntity(FhirContext ctx, Resource resourceToCreate) {

    // Create the FHIR store URL.
    String fhirStoreUrl = String.format("%s/fhir", FHIR_STORE_NAME);

    // Create a BearerTokenAuthInterceptor object.
    BearerTokenAuthInterceptor authInterceptor = null;
    try {
      authInterceptor = new BearerTokenAuthInterceptor(getAccessToken());
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }

    // Register the auth interceptor with the FHIR client.
    IGenericClient HAPIFhirClient = ctx.newRestfulGenericClient(fhirStoreUrl);
    HAPIFhirClient.registerInterceptor(authInterceptor);

    // Use the client to store a new resource instance.
    MethodOutcome outcome = HAPIFhirClient
        .create()
        .resource(resourceToCreate)
        .execute();

    // Return true if the resource was created successfully, false otherwise.
    return outcome.getCreated();

  }

  /**
   * Gets an access token for the current project.
   *
   * @return The access token.
   * @throws IOException if the request fails.
   */
  private String getAccessToken() throws IOException {
    GoogleCredentials credential = GoogleCredentials.getApplicationDefault()
        .createScoped(Collections.singleton(CloudHealthcareScopes.CLOUD_PLATFORM));

    return credential.refreshAccessToken().getTokenValue();
  }

}
