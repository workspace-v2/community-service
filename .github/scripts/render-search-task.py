#!/usr/bin/env python3
"""Apply explicit search settings while preserving every other ECS task setting."""
import argparse
import json
import os
import re
import sys
from urllib.parse import urlsplit


def validate_settings(engine, url='', prefix='', password_parameter=''):
    if engine not in ('mysql', 'elasticsearch'):
        raise ValueError('SEARCH_ENGINE must be mysql or elasticsearch')
    if engine == 'elasticsearch':
        parsed = urlsplit(url)
        if parsed.scheme not in ('http', 'https') or not parsed.hostname or parsed.username or parsed.password or parsed.query or parsed.fragment:
            raise ValueError('An Elasticsearch URL without credentials is required')
        if not re.fullmatch(r'burger-[a-z0-9-]+', prefix):
            raise ValueError('A valid environment-specific index prefix is required')
        if not password_parameter or not password_parameter.startswith(('arn:aws:ssm:', '/burger/search/')):
            raise ValueError('An SSM reader password reference is required')


def configure(task, engine, url='', prefix='', password_parameter=''):
    validate_settings(engine, url, prefix, password_parameter)
    apps = [container for container in task['containerDefinitions'] if container['name'] == 'app']
    if len(apps) != 1:
        raise ValueError('Expected exactly one app container')
    app = apps[0]
    names = {'SEARCH_ENGINE', 'ELASTICSEARCH_URL', 'ELASTICSEARCH_USERNAME', 'ELASTICSEARCH_INDEX_PREFIX', 'ELASTICSEARCH_PASSWORD'}
    app['environment'] = [item for item in app.get('environment', []) if item['name'] not in names]
    app['secrets'] = [item for item in app.get('secrets', []) if item['name'] not in names]
    app['environment'].append({'name': 'SEARCH_ENGINE', 'value': engine})
    if engine == 'elasticsearch':
        app['environment'].extend({'name': name, 'value': value} for name, value in {
            'ELASTICSEARCH_URL': url, 'ELASTICSEARCH_USERNAME': 'burger_search', 'ELASTICSEARCH_INDEX_PREFIX': prefix}.items())
        app['secrets'].append({'name': 'ELASTICSEARCH_PASSWORD', 'valueFrom': password_parameter})
    return task


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--validate-only', action='store_true', help='validate deployment settings without reading an ECS task')
    args = parser.parse_args()
    settings = (os.environ.get('SEARCH_ENGINE', 'mysql'), os.environ.get('ELASTICSEARCH_URL', ''),
                os.environ.get('ELASTICSEARCH_INDEX_PREFIX', ''), os.environ.get('ELASTICSEARCH_PASSWORD_PARAMETER', ''))
    try:
        validate_settings(*settings)
    except ValueError as error:
        parser.exit(1, 'Invalid search deployment configuration: ' + str(error) + '\n')
    if args.validate_only:
        print('Search deployment configuration valid')
        return
    json.dump(configure(json.load(sys.stdin), *settings), sys.stdout)


if __name__ == '__main__':
    main()
