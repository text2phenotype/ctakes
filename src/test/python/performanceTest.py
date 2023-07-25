# Input arguments:
#    -inputDir: path to the folder with test texts
#    -outputDir: path to the folder for saving output JSON files and result.bsv

import os
import sys
import requests
import time
import hashlib

from pip import logger

endpoints = {
    "hepc_lab_value": "http://198.199.106.70:8080/hepc/pipeline/lab_value",
    "hepc_drug_ner": "http://198.199.106.70:8080/hepc/pipeline/drug_ner",
    "hepc_temporal_module": "http://198.199.106.70:8080/hepc/pipeline/temporal_module",
    "hepc_default_clinical": "http://198.199.106.70:8080/hepc/pipeline/default_clinical",

    "cpt_drug_ner": "http://198.199.106.70:8080/cpt/pipeline/drug_ner",
    "cpt_temporal_module": "http://198.199.106.70:8080/cpt/pipeline/temporal_module",
    "cpt_default_clinical": "http://198.199.106.70:8080/cpt/pipeline/default_clinical",

    "loinc_drug_ner": "http://198.199.106.70:8080/loinc/pipeline/drug_ner",
    "loinc_temporal_module": "http://198.199.106.70:8080/loinc/pipeline/temporal_module",
    "loinc_default_clinical": "http://198.199.106.70:8080/loinc/pipeline/default_clinical",

    "rxnorm_drug_ner": "http://198.199.106.70:8080/rxnorm/pipeline/drug_ner",
    "rxnorm_temporal_module": "http://198.199.106.70:8080/rxnorm/pipeline/temporal_module",
    "rxnorm_default_clinical": "http://198.199.106.70:8080/rxnorm/pipeline/default_clinical",

    "snomedct_drug_ner": "http://198.199.106.70:8080/snomedct/pipeline/drug_ner",
    "snomedct_temporal_module": "http://198.199.106.70:8080/snomedct/pipeline/temporal_module",
    "snomedct_default_clinical": "http://198.199.106.70:8080/snomedct/pipeline/default_clinical",

    "general_lab_value": "http://198.199.106.70:8080/general/pipeline/lab_value",
    "general_temporal_module": "http://198.199.106.70:8080/general/pipeline/temporal_module",
    "general_pos_tagger": "http://198.199.106.70:8080/general/pipeline/pos_tagger"
}

# parse arguments
params = {}
for i in range(1, len(sys.argv), 2):
    params[sys.argv[i]] = sys.argv[i+1]

if '-inputDir' not in params:
    raise Exception('The input directory is not defined')

if '-outputDir' not in params:
    raise Exception('The output directory is not defined')

inputDir = params.get('-inputDir')
outputDir = params.get('-outputDir')

if not os.path.exists(outputDir):
    logger.info('creating outputDir %s ' % outputDir)
    os.makedirs(outputDir)


def get_current_milli_sec():
    """
    Get current ms
    :return: time in ms
    """
    return int(round(time.time() * 1000))


def cache_key(text):
    """
    Get hashkey for text
    :param text: biomedical text
    :return: unique key
    """
    return hashlib.md5(text.encode()).hexdigest()


def cache_file(text, cache_type):
    """
    :param text: input file name
    :param cache_type: endpoint name
    :return:
    """
    if not os.path.exists(outputDir):
        logger.info('creating outputDir %s ' % outputDir)
        os.makedirs(outputDir)

    _key = cache_key(text)
    _file = os.path.join(outputDir, _key+cache_type+'.json')

    return _file


# perform requests and save responses

bsv_file_name = os.path.join(outputDir, 'result.bsv')


for (dirpath, dirnames, filenames) in os.walk(inputDir):
    print('{0} file(s) for testing'.format(len(filenames)))
    for filename in filenames:

        filePath = os.path.join(dirpath, filename)

        fileSize = os.path.getsize(filePath)
        with open(filePath, 'r') as f:

            try:
                inputText = f.read()

                print('File {0}:'.format(filename))
                for endpointName in endpoints:
                    url = endpoints.get(endpointName)
                    data = {'inputText': inputText}
                    beginTime = get_current_milli_sec()
                    response = requests.post(url, data)
                    endTime = get_current_milli_sec()

                    # add line to BSV file
                    with open(bsv_file_name, 'a') as resultBSV:
                        resultBSV.write("{0}|{1}|{2}|{3}_{4}|{5}\n".format(
                            beginTime,
                            endTime,
                            fileSize,
                            filename,
                            endpointName,
                            response.status_code == 200
                        ))

                    # save result to json file
                    json_file_name = cache_file(filename, endpointName)

                    if '-noJson' not in params:
                        with open(json_file_name, 'w') as resultJson:
                            resultJson.write(response.text)

                    print('\t{0} - {1} ({2} ms)'.format(endpointName, response.status_code, endTime - beginTime))
            except KeyboardInterrupt:
                print("Test stopped")
                exit(0)
            else:
                pass

print("Test finished")
