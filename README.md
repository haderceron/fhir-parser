# FHIR Parser

This is a simple FHIR parser that can create and parse FHIR resources.

## Usage

To use the parser, first create a new instance of the `FhirParser` class. Then, call the `parseFhirResource()` method with the request body containing the FHIR resource. The method will return the parsed FHIR resource.

To create a FHIR resource, first create a new instance of the `Resource` class. Then, set the properties of the resource. Finally, call the `createFHIREntity()` method with the resource.

## Example

Here is an example of how to use the parser:

FhirParser parser = new FhirParser(); String requestBody = "{"resourceType": "Patient", "name": [{"use": "official", "family": "Smith", "given": ["Darcy"]}]}"; String parsedResource = parser.parseFhirResource(requestBody); System.out.println(parsedResource);

Patient patient = new Patient(); patient.setName(new HumanName().setFamily("Smith").setGiven(Arrays.asList("Darcy"))); parser.createFHIREntity(patient);

## Contributing

If you would like to contribute to the parser, please fork the repository and submit a pull request.

## License

The parser is licensed under the Apache License, Version 2.0.
