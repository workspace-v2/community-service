import copy
import importlib.util
from pathlib import Path
import unittest

spec=importlib.util.spec_from_file_location('search_task',Path(__file__).resolve().parents[1]/'render-search-task.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)

class SearchTaskTest(unittest.TestCase):
    def task(self):
        return {'family':'burger-dev','cpu':'1024','containerDefinitions':[
            {'name':'app','image':'unchanged','environment':[{'name':'SPRING_DATASOURCE_URL','value':'jdbc:mysql://host/custom_snapshot'},
            {'name':'ELASTICSEARCH_PASSWORD','value':'old-insecure-value'}],
            'secrets':[{'name':'DB_PASSWORD','valueFrom':'db-param'}]},
            {'name':'sidecar','image':'untouched'}]}
    def test_elastic_settings_preserve_db_image_resources_and_sidecar(self):
        original=self.task(); configured=m.configure(copy.deepcopy(original),'elasticsearch','http://10.20.1.167:9200','burger-dev','/burger/search/reader_password')
        self.assertEqual(configured['cpu'], original['cpu'])
        self.assertEqual(configured['containerDefinitions'][1],original['containerDefinitions'][1])
        app=configured['containerDefinitions'][0]
        self.assertEqual(app['image'],'unchanged')
        env={item['name']:item['value'] for item in app['environment']}
        self.assertEqual(env['SPRING_DATASOURCE_URL'],'jdbc:mysql://host/custom_snapshot')
        self.assertNotIn('ELASTICSEARCH_PASSWORD',env)
        self.assertEqual(app['secrets'][-1],{'name':'ELASTICSEARCH_PASSWORD','valueFrom':'/burger/search/reader_password'})
    def test_mysql_removes_only_search_secret_settings(self):
        task=m.configure(self.task(),'elasticsearch','http://host:9200','burger-dev','/burger/search/reader_password')
        app=m.configure(task,'mysql')['containerDefinitions'][0]
        self.assertEqual(app['secrets'],[{'name':'DB_PASSWORD','valueFrom':'db-param'}])
        self.assertEqual([v for v in app['environment'] if v['name'].startswith('ELASTICSEARCH')],[])
    def test_missing_config_and_credentials_in_url_are_rejected(self):
        for url,prefix,password in [('', 'burger-dev','param'),('http://u:p@host:9200','burger-dev','/burger/search/reader_password'),
                                     ('http://host','wrong','/burger/search/reader_password')]:
            with self.assertRaises(ValueError):m.configure(self.task(),'elasticsearch',url,prefix,password)
        with self.assertRaises(ValueError):m.configure(self.task(),'typo')

if __name__=='__main__':unittest.main()
