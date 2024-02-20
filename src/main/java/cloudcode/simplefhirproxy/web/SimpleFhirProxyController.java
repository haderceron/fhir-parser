package cloudcode.simplefhirproxy.web;

import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import ca.uhn.fhir.context.FhirContext;
import cloudcode.simplefhirproxy.SimpleFhirProxy;

/**
 * Defines a controller to handle HTTP requests
 *
 * @author haderceron
 */
@Controller
public final class SimpleFhirProxyController {

  private static final Logger logger = LoggerFactory.getLogger(SimpleFhirProxyController.class);

  /**
   * Create an endpoint for the landing page
   *
   * @return the index view template
   */
  @GetMapping("/")
  public String helloWorld(Model model) {

    // Get Cloud Run environment variables.
    String revision = System.getenv("K_REVISION") == null ? "???" : System.getenv("K_REVISION");
    String service = System.getenv("K_SERVICE") == null ? "???" : System.getenv("K_SERVICE");

    // Set variables in html template.
    model.addAttribute("revision", revision);
    model.addAttribute("service", service);
    return "index";
  }

/**
 * Create an endpoint to process FHIR resources
 *
 * @param body the FHIR resource in JSON format
 * @return a message indicating whether the resource was created or not
 */
@PostMapping("/fhir-proxy")
@ResponseBody
public String processFhirResource(@RequestBody String body) {

  // The FHIR context used to parse and generate FHIR resources.
  FhirContext ctx = FhirContext.forR4();

  // The SimpleFhirProxy object used to preprocess and create FHIR resources.
  final SimpleFhirProxy simpleFhirProxy = new SimpleFhirProxy();

  // The FHIR resource parsed from the request body.
  Resource fhirResource = simpleFhirProxy.preprocessFhirResource(ctx, body);

  // If the FHIR resource is null, then it is not allowed.
  if (fhirResource == null) return "Resource Not Allowed";
  logger.info("Resource Not Allowed");

  // The response message indicating whether the resource was created or not.
  String responseMessage = simpleFhirProxy.createFHIREntity(ctx, fhirResource) ? "Resource Created" : "Resource Not Created";
  logger.info(responseMessage);

  // The response message is returned.
  return responseMessage;
}


}

