import json
import os
import requests
import unittest


class TestBiomed508(unittest.TestCase):

    NLP_HOST = os.environ.get('NLP_HOST')

    url = NLP_HOST + '/hepc/default_clinical'
    headers = {
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
    }

    input_text = 'BUN'

    def doRequest(self, data):
        response = requests.post(self.url, data, headers=self.headers)
        return response.status_code, json.loads(response.text)

    def test_unused_attributes(self):
        """
        Test if unused attributes are removed
        :return:
        """
        data = {
            'inputText': self.input_text
        }

        response = requests.post(self.url, data, headers=self.headers)
        json_data = json.loads(response.text)
        self.assertTrue(len(json_data['content']) > 0)
        for item in json_data['content']:
            attributes = item['attributes']
            self.assertNotIn('generic', attributes, '"generic" attribute has to be removed from the attributes')
            self.assertNotIn('conditional', attributes, '"conditional" attribute has to be removed from the attributes')
            self.assertNotIn('modality', attributes, '"modality" attribute has to be removed from the attributes')
            self.assertNotIn('uncertainty', attributes, '"uncertainty" attribute has to be removed from the attributes')
            self.assertNotIn('relTime', attributes, '"relTime" attribute has to be removed from the attributes')
