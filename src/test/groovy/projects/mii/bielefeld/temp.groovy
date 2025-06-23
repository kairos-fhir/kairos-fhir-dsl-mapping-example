package projects.mii.bielefeld

def studyProfileCode = 'OWL_DIZ_STUDYPROF_EIGENF_TEMPLATE'
def studyCode = 'OWL_DIZ_STUDY_EIGENF_{{haus_id_uc}}'
def consentTypeCode = 't_eigenf_{{haus_id}}'
def flexIdKey = 'Patient.identifier:pid'
def now = java.time.ZonedDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
def patients = path('EffectData.PatientDataSet')
def pattern = ~/"key":"${flexIdKey}","value":"(.*?)"/

for (int i = patients.size() - 1; i >= 0; --i) {

    def firstName = path("EffectData.PatientDataSet[$i].Masterdata.FirstName").value()
    def lastName = path("EffectData.PatientDataSet[$i].Masterdata.LastName").value()
    def date = path("EffectData.PatientDataSet[$i].Masterdata.DateOfBirth.Date").value()

    println("INGEST-RULE: Add patient to study: " + firstName + "; " + lastName + "; " + date)

    if (firstName && lastName && date) {

        def uuid = UUID.randomUUID().toString()
        // fallback
        def uuidSync = uuid
        def birthDate = now

        def patient = patients?.get(i)

        if (patient != null) {
            def idContainer = patient?.IDContainer
            //  def masterData = patient?.Masterdata
            if (idContainer != null) {
                def rawFlexibleID = idContainer?.FlexibleID
                def flexList = []
                if (rawFlexibleID instanceof List) {
                    flexList = rawFlexibleID
                } else if (rawFlexibleID != null) {
                    flexList = [rawFlexibleID]
                }
                for (def item in flexList) { // pattern
                    def str = item.toString()
                    def matcher = pattern.matcher(str)
                    if (matcher.find()) {
                        uuidSync = matcher.group(1).trim()
                        break
                    }
                }
            }

            if (masterData != null) {
                def dateOfBirth = masterData?.DateOfBirth
                if (dateOfBirth != null) {
                    def d = dateOfBirth?.Date
                    if (d != null) {
                        def birthDateTemp = d.value.toString()
                        if (birthDateTemp instanceof String && birthDateTemp.startsWith('"') && birthDateTemp.endsWith('"')) {
                            birthDate = birthDateTemp.replaceAll('^"|"$', '')
                        }
                    }
                }
            }

            if (path("EffectData.PatientDataSet[$i].PatientStudy").isNull()) {
                path("EffectData.PatientDataSet[$i]").asObject().set('PatientStudy', [])
            }
            path("EffectData.PatientDataSet[$i].PatientStudy").asArray().add([
                    '__class'      : 'de.kairos.centraxx.common.xml.exchange.PatientFlexiStudyType',
                    '__attributes' : [:],
                    'MemberFrom'   : [
                            '__class'     : 'de.kairos.centraxx.common.xml.exchange.DateType',
                            '__attributes': [:],
                            'Date'        : ['value': now],
                            'Precision'   : 'EXACT'
                    ],

                    'FlexiStudyRef': [
                            '__class'        : 'de.kairos.centraxx.common.xml.exchange.FlexiStudyRefType',
                            '__attributes'   : [:],
                            'StudyCode'      : studyCode,
                            'StudyProfileRef': [
                                    '__class'     : 'de.kairos.centraxx.common.xml.exchange.CatalogueDataRefType',
                                    '__attributes': [:],
                                    'value'       : studyProfileCode
                            ]
                    ],
                    'ConsentRef'   : uuid,
            ])

            if (path("EffectData.PatientDataSet[$i].Consent").isNull()) {
                path("EffectData.PatientDataSet[$i]").asObject().set('Consent', [])
            }
            path("EffectData.PatientDataSet[$i].Consent").asArray().add([
                    '__class'         : 'de.kairos.centraxx.common.xml.exchange.ConsentType',
                    '__attributes'    : [:],
                    'ConsentRef'      : uuid,
                    'ConsentType'     : [
                            '__class'     : 'de.kairos.centraxx.common.xml.exchange.CatalogueDataRefType',
                            '__attributes': [:],
                            'value'       : consentTypeCode
                    ],
                    'ValidFrom'       : [
                            '__class'     : 'de.kairos.centraxx.common.xml.exchange.DateType',
                            '__attributes': [:],
                            'Date'        : ['value': birthDate],
                            'Precision'   : 'EXACT'
                    ],
                    'ConsentPartsOnly': false,
                    'Declined'        : false,
                    'SyncId'          : uuidSync,
            ])
        }
    }
}
