# Contributing
We like contributions, Thanks for taking time to contribute and helping us make this project better! The following is a set of guidelines for contributing to crAPI. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

When contributing to this repository, please first discuss the change you wish to make via issue,
or `#crapi` channel on [OWASP slack](https://join.slack.com/t/owasp/shared_invite/zt-18rwlvzj6-BmIjvJj9gW6QUQG0fEusMQ) with the owners of this repository before making a change.
 
Please note we have a code of conduct, please follow it in all your interactions with the project.
 
## Submitting bugs
 
### Due diligence
-------------
 
Before submitting a bug, please do the following:
 
* Perform **basic troubleshooting** steps:
 
* **Search the GitHub issues** to make sure it's not a known
 issue.
* If you don't find a pre-existing issue, consider **checking with the slack channel** in case the problem is non-bug-related.
 
### What to put in your issue
------------------------------
 
Make sure issue you raised gets the attention it deserves: issues with missing
information may be ignored or punted back to you, delaying a fix. The below
constitutes a bare minimum; more info is almost always better:
 
* **What operating system are you on?** Windows? (10? Home? Pro?)
 Mac OS X?  (10.x.x?) Linux? (Which distro? Which version of that
 distro? 32 or 64 bits?) Again, more detail is better.
* **Are you using latest codebase and which source are you using? Main or develop branch or image tag?**
Did you build your own images or used public images? If yes what 
Ideally, you
 followed the advice above and have ruled out (or verified that the problem
 exists in) a few different versions.
* **How can the developers recreate the bug on their end?** If possible,
 include a copy of your code or an example repo, the command you used to invoke it,
 and the full output of your run (if applicable.)
 
### Version control branching
-------------------------
 
* Always **make a new branch** for your work, no matter how small. This makes
 it easy for others to take just that one set of changes from your repository,
 in case you have multiple unrelated changes floating around.
 
   * A corollary: **don't submit unrelated changes in the same branch/pull
     request**! The maintainer shouldn't have to reject your awesome bugfix
     because the feature you put in with it needs more review.
 
* **Base your new branch off of the appropriate branch** on the main
 repository:
 
   * **Bug fixes** should be based on the branch named after the **oldest
     supported release line** the bug affects.
 
       * E.g. if a feature was introduced in v1.1, the latest release line is
         v1.3, and a bug is found in that feature - make your branch based on
         v1.1.  The maintainer will then forward-port it to v1.3 and master.
       * Bug fixes requiring large changes to the code or which have a chance
         of being otherwise disruptive, may need to base off of **main**
         instead. This is a judgement call -- ask the devs!
 
   * **New features** should branch off of **the 'main' branch**.
 
       * Note that depending on how long it takes for the dev team to merge
         your patch, the copy of ``main`` you worked off of may get out of
         date! If you find yourself 'bumping' a pull request that's been
         sidelined for a while, **make sure you rebase or merge to latest
         main** to ensure a speedier resolution.
 
### Tests aren't optional
---------------------
Any bugfix that doesn't include a test proving the existence of the bug being
fixed, may be suspect.  Ditto for new features that can't prove they actually
work.
 
We've found that test-first development really helps make features better
architected and identifies potential edge cases earlier instead of later.
Writing tests before the implementation is strongly encouraged.
 
## Full example
------------
 
Here's an example workflow for a project `theproject` hosted on Github, which
is currently in version v1.0.x. Your username is `yourname` and you're
submitting a basic bugfix. (This workflow only changes slightly if the project
is hosted at Bitbucket, self-hosted, or etc.)
 
### Preparing your Fork
 
 
1. Click 'Fork' on Github, creating e.g. `yourname/theproject`.
2. Clone your project: `git clone git@github.com:yourname/theproject`.
3. `cd theproject`
4. Create a branch: `git checkout -b foo-the-bars v1.0`.
 
### Making your Changes
 
1. Write tests expecting the correct/fixed functionality; make sure they fail.
2. Hack, hack, hack.
3. Run tests again, making sure they pass.
4. Commit your changes: `git commit -m "Foo the bars"`
 
### Creating Pull Requests
 
 
1. Push your commit to get it back up to your fork: `git push origin HEAD`
2. Visit Github, click handy "Pull request" button that it will make upon
  noticing your new branch.
3. In the description field, write down issue number (if submitting code fixing
  an existing issue) or describe the issue + your fix (if submitting a wholly
  new bugfix).
4. Hit 'submit'! And please be patient - the maintainers will get to you when
  they can.
 
## Support Channels
---
Whether you are a user or contributor, official support channels include:
- GitHub issues: https://github.com/owasp/crapi/issues/new
- Slack: `#crapi` channel in [OWASP slack](https://join.slack.com/t/owasp/shared_invite/zt-18rwlvzj6-BmIjvJj9gW6QUQG0fEusMQ)
 
## Sources
---
 
Currently this document draws from the contribution documentation for a handful
of some open source projects: [Fabric](http://fabfile.org), [Invoke](http://pyinvoke.org), [Paramiko](http://paramiko.org), etc.
 