package cloudcode.helloworld.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import cloudcode.helloworld.FhirParser;

/** Defines a controller to handle HTTP requests */
@Controller
public final class FhirParserController {

  private static String project;
  private static final Logger logger = LoggerFactory.getLogger(FhirParserController.class);

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
   * Parse a FHIR resource
   *
   * @param fhirResource the FHIR resource to parse
   * @return the parsed FHIR resource
   */
  @PostMapping("/fhir-parser")
  @ResponseBody
  public String parseFhirResource(@RequestBody String fhirResource) {

    // Parse the FHIR resource.
    final FhirParser parser = new FhirParser();
    try {
      fhirResource = parser.parseFhirResource(fhirResource);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Return the parsed FHIR resource.
    return fhirResource;
  }

}
