"""Deployment status polling must survive a transient AWS endpoint failure."""
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / "deploy-burger.sh"


class DeploymentPollingTest(unittest.TestCase):
    def test_status_lookup_retries_without_failing_a_successful_deployment(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            aws = root / "aws"
            aws.write_text('''#!/usr/bin/env python3
import json, os, sys
from pathlib import Path
args = sys.argv[1:]
if 'describe-task-definition' in args:
 print(json.dumps({'family':'burger-dev','containerDefinitions':[{'name':'app','image':'old'}]}))
elif 'register-task-definition' in args:
 print('arn:aws:ecs:region:account:task-definition/burger-dev:2')
elif 'describe-services' in args:
 print(json.dumps({'failures':[],'services':[{'desiredCount':1}]}))
elif 'create-deployment' in args:
 print('d-test')
elif 'get-deployment' in args:
 state = Path(os.environ['POLL_TEST_STATE'])
 if not state.exists():
  state.write_text('retried')
  print('Temporary endpoint unavailable', file=sys.stderr)
  sys.exit(255)
 print('Succeeded')
else:
 sys.exit(1)
''')
            aws.chmod(0o755)
            sleep = root / "sleep"
            sleep.write_text("#!/bin/sh\nexit 0\n")
            sleep.chmod(0o755)
            env = dict(os.environ, PATH=str(root) + os.pathsep + os.environ["PATH"],
                       POLL_TEST_STATE=str(root / "state"), IMAGE="test", TASK_FAMILY="burger-dev",
                       ECS_CLUSTER="burger", ECS_SERVICE="burger-dev", CODEDEPLOY_APPLICATION="burger",
                       CODEDEPLOY_GROUP="burger-dev", DEPLOY_ENV="dev")
            env.pop("GITHUB_STEP_SUMMARY", None)
            result = subprocess.run(["bash", str(SCRIPT)], env=env, capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn("retrying deployment d-test", result.stderr)
            self.assertIn("deployment succeeded", result.stdout)


if __name__ == "__main__":
    unittest.main()
