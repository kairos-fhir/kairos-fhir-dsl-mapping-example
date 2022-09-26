package projects.cxx.custom.exceptions

import de.kairos.fhir.centraxx.metamodel.RootEntities

import java.nio.charset.StandardCharsets

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 */
patient {

  final String logicalId = context.source[RootEntities.patientMasterDataAnonymous().patientContainer().id()]

  id = "Patient/" + logicalId
  gender = getGenderFromExternalFile(logicalId)

}

/**
 * Reads the gender from an external file. This is just an example of exception handling with external resources.
 * @param logicalId the logical FHIR ID of a patient
 * @return the name of the FHIR gender from file
 */
static String getGenderFromExternalFile(final String logicalId) {

  final File file = new File("C:/centraxx-home/fhir-custom-mappings/experiment/OidToGender.csv")
  try {
    return file.newInputStream().withCloseable {
      final List<String> lines = it.readLines(StandardCharsets.UTF_8.name())
      for (final String line : lines) {
        final List<String> oidToGender = Arrays.asList(line.split(","))
        if (oidToGender.get(0) == logicalId) {
          return oidToGender.get(1)
        }
      }
      // Exception raise is just an example for error handling. Do not use it in production!
      throw new IllegalStateException("OID '" + logicalId + "' in file not found.")
    }
  } catch (final FileNotFoundException | IllegalStateException ex) {
    ex.printStackTrace()
    return "unknown"
  }
}
