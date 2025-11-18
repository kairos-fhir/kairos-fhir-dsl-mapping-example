# Examples

## Source Message

1. The source message is transformed using the Groovy script. It contains dummy data structured according to the ISIK FHIR profiles.
2. The transformed bundle is the output produced by the Groovy transformation.

## Transformation Logic

The incoming message includes one Patient and one Condition resource. These ISIK resources are transformed into three HDRP resources: 
Patient, Condition, and Observation.

To correctly link these resources in HDRP, appropriate references must be used. Since records may be sent to HDRP multiple times, 
they should be created if they do not exist, or updated using a business identifier if they already exist.

HDRP Bundle import deviates from the standard FHIR specifications. It supports POST, PUT, and DELETE operations.
Both POST and PUT follow a create-if-not-exists or update logic. For POST, HDRP uses a business key to resolve records in its database. 
For a Patient, this is typically the `Patient.identifier`. In some cases, business keys consist of multiple resource elements, 
which may include fields outside the identifier element. We are working on profiling these natural identifier fields in separate
StructureDefinitions on Simplifier.net.

When using PUT, HDRP resolves records by the logical FHIR ID provided in the resource.