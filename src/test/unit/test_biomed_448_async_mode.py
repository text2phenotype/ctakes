import json
import os
import requests
import unittest


class TestBiomed448(unittest.TestCase):
    NLP_HOST = os.environ.get('NLP_HOST')
    url = NLP_HOST + '/hepc/default_clinical'

    headers = {
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
    }

    input_text = 'BUN'

    def doRequest(self, data):
        response = requests.post(self.url, data, headers=self.headers)
        return response.status_code, json.loads(response.text)

    def test_async_on(self):
        """
        Test if async option is True
        :return:
        """

        data = {
            'inputText': self.input_text,
            'async': True
        }
        code, json_post = self.doRequest(data)
        self.assertEqual(202, code)

        self.assertIn('id', json_post)

        id = json_post['id']

        data_get = {
            'id': id
        }
        response = requests.post(self.url, data_get, headers=self.headers)
        self.assertEqual(200, response.status_code)
        json_get = json.loads(response.text)
        self.assertIn('content', json_get)

    def test_async_off(self):
        """
        Test if async option is False
        :return:
        """

        data = {
            'inputText': self.input_text,
            'async': False
        }
        code, json_post = self.doRequest(data)
        self.assertEqual(200, code)
        self.assertIn('content', json_post)

    def test_async_default(self):
        """
        Test if async option is not defined
        :return:
        """

        data = {
            'inputText': self.input_text
        }
        code, json_post = self.doRequest(data)
        self.assertEqual(200, code)
        self.assertIn('content', json_post)
