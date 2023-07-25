import requests, os

# URL
service_url = '<Service url>'

# Available dictionaries.
dictionaries = ['cpt', 'loinc', 'rxnorm', 'snomedct', 'hepc']

# Endpoints.
endpoints = [
    '/pipeline/plain_text',         # endpoint includes all services
    '/pipeline/default_clinical',   # Default clinical pipeline
    '/pipeline/clinical_document',  # Clinical document pipeline
    '/pipeline/temporal_module',    # Temporal module
    '/pipeline/drug_ner',           # Drug NER
    '/pipeline/smoking_status'      # Smoking status
]

# Request URL. You can change dictionary and endpoint
url = '{0}/{1}{2}'.format(service_url, dictionaries[0], endpoints[0])


def read_file(file_path):
    """Reads file content"""
    result = None
    if os.path.exists(file_path):
        with open(file_path) as fp:
            result = fp.read()
    else:
        print("File isn't exists")

    return result


def run_clinical_pipeline_text(text_input_file):
    """Performs plain text request"""
    # Text to analyze
    text = read_file(text_input_file)
    # Data type. Available values: plain_text, ccda
    datatype = 'plain_text'
    # Request data
    data = {'inputText': text, 'datatype': datatype}
    print "Start request..."
    response = requests.post(url, data=data)
    print response.text


def run_clinical_pipeline_xml(xml_input_file):
    """Performs a request using CCDA data"""
    # Text to analyze
    text = read_file(xml_input_file)
    # Data type. Available values: plain_text, ccda
    datatype = 'ccda'
    # Request data
    data = {'inputText': text, 'datatype': datatype}
    print "Start request..."
    response = requests.post(url, data=data)
    print response.text

run_clinical_pipeline_text('<path to the input text file>')
