import json
import os
import requests
import unittest


class TestBiomed540(unittest.TestCase):

    NLP_HOST = os.environ.get('NLP_HOST')
    url = NLP_HOST + '/hepc/lab_value'
    headers = {
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
    }
    input_text = 'AFP <1.0'

    def doRequest(self, data):
        response = requests.post(self.url, data, headers=self.headers)
        return response.status_code, json.loads(response.text)

    def test_with_less_symbol(self):
        data = {
            'inputText': 'AFP <1.0'
        }
        response = requests.post(self.url, data, headers=self.headers)
        json_data = json.loads(response.text)
        item = json_data['labValues'][0]
        self.assertEqual('AFP', item['text'][0])
        self.assertEqual('<1.0', item['attributes']['labValue'][0])

    def test_with_more_symbol(self):
        data = {
            'inputText': 'AFP >15.0'
        }
        response = requests.post(self.url, data, headers=self.headers)
        json_data = json.loads(response.text)
        item = json_data['labValues'][0]
        self.assertEqual('AFP', item['text'][0])
        self.assertEqual('>15.0', item['attributes']['labValue'][0])

    def test_without_symbol(self):
        data = {
            'inputText': 'AFP 26.0'
        }
        response = requests.post(self.url, data, headers=self.headers)
        json_data = json.loads(response.text)
        item = json_data['labValues'][0]
        self.assertEqual('AFP', item['text'][0])
        self.assertEqual('26.0', item['attributes']['labValue'][0])
