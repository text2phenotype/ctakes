
import os
import sys
import requests
import json
import logging
from builtins import *

ignoredProps = {'docId', 'umlsConcept', 'jira', 'user', 'timestamp', 'date', 'version'}
# serverUrl = 'http://198.199.106.70:8080/ctakes/rest'
serverUrl = 'http://127.0.0.1:8080/ctakes/rest'
endpoints = {
    # "hepc_lab_value": "/hepc/lab_value",
    # "hepc_drug_ner": "/hepc/pipeline/drug_ner",
    # "hepc_temporal_module": "/hepc/pipeline/temporal_module",
    # "hepc_default_clinical": "/hepc/pipeline/default_clinical",
    #
    # "cpt_drug_ner": "/cpt/pipeline/drug_ner",
    # "cpt_temporal_module": "/cpt/pipeline/temporal_module",
    # "cpt_default_clinical": "/cpt/pipeline/default_clinical",
    #
    # "loinc_drug_ner": "/loinc/pipeline/drug_ner",
    # "loinc_temporal_module": "/loinc/pipeline/temporal_module",
    # "loinc_default_clinical": "/loinc/pipeline/default_clinical",
    #
    # "rxnorm_drug_ner": "/rxnorm/pipeline/drug_ner",
    # "rxnorm_temporal_module": "/rxnorm/pipeline/temporal_module",
    # "rxnorm_default_clinical": "/rxnorm/pipeline/default_clinical",
    #
    # "snomedct_drug_ner": "/snomedct/pipeline/drug_ner",
    # "snomedct_temporal_module": "/snomedct/pipeline/temporal_module",
    # "snomedct_default_clinical": "/snomedct/pipeline/default_clinical",
    #
    # "general_lab_value": "/general/pipeline/lab_value",
    # "general_temporal_module": "/general/pipeline/temporal_module",
    # "general_pos_tagger": "/general/pipeline/pos_tagger"
    #
    "shrine_icd9_default_clinical": "/shrine/icd9/default_clinical"
}

logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger('CITest')

# parse arguments
params = {}
for i in range(1, len(sys.argv), 2):
    params[sys.argv[i]] = sys.argv[i + 1]

if '-workingDir' not in params:
    raise Exception('The working directory is not defined')

workingDir = params.get('-workingDir')


def check_response(actual_json, gold_json):
    if isinstance(actual_json, dict):
        for prop in actual_json:
            if prop in ignoredProps:
                continue

            l_res, l_error = check_response(actual_json.get(prop), gold_json.get(prop))
            if not l_res:
                return False, '{0}/{1}'.format(prop, l_error)

    elif isinstance(actual_json, list):
        if len(actual_json) != len(gold_json):
            return False, ''

        for idx in range(0, len(actual_json)):
            l_res, l_error = check_response(actual_json[idx], gold_json[idx])
            if not l_res:
                return False, 'Item{1}/{0}'.format(l_error, idx)
    else:
        if actual_json is None and gold_json is None:
            return True, None

        res = actual_json == gold_json
        if not res:
            return res, ''
        return res, None

    return True, None


for endpointName in endpoints:
    endpointDirPath = os.path.join(workingDir, endpointName)

    if not os.path.exists(endpointDirPath):

        logger.warning("Endpoint directory of '%s' is not exist", endpointName)
        continue

    # check requests directory
    requestEndpointDirPath = os.path.join(endpointDirPath, 'request')
    if not os.path.exists(requestEndpointDirPath):
        logger.error("Endpoint request directory of '%s' is not exist", endpointName)
        break

    # check responses directory
    responseEndpointDirPath = os.path.join(endpointDirPath, 'response')
    if not os.path.exists(responseEndpointDirPath):
        logger.error("Endpoint response directory of '%s' is not exist", endpointName)
        break

    url = serverUrl + endpoints.get(endpointName)

    for dirpath, dirnames, filenames in os.walk(requestEndpointDirPath):
        for filename in filenames:
            requestDataFile = os.path.join(requestEndpointDirPath, filename)
            responseDataFile = os.path.join(responseEndpointDirPath, filename)

            if not os.path.exists(responseDataFile):
                logger.error("Response file '%s' is not found", responseDataFile)
                exit(0)
                break

            with open(requestDataFile, 'r') as requestF:
                with open(responseDataFile, 'r') as responseF:
                    try:
                        inputText = requestF.read()
                        goldJSON = json.loads(responseF.read())

                        data = {'inputText': inputText}

                        response = requests.post(url, data)

                        if response.status_code != 200:
                            logger.error("Url is unavailable or fail: '%s'", url)
                            exit(0)

                        actualJSON = json.loads(response.text)

                        res, error = check_response(actualJSON, goldJSON)
                        if res:
                            logger.info("Test for %s ... OK", filename)
                        else:
                            logger.error("Test for %s ... Failed (%s)", filename, error)
                            exit(0)

                    except KeyboardInterrupt:
                        logger.info("Test is stopped by user")
                        exit(0)
                    except Exception as e:
                        logger.error(e.args)
                        exit(0)

logger.info("Test finished")
