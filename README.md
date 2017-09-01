TeamCity Autotools Plug-in
==========================
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://api.travis-ci.org/unix-junkie/teamcity-autotools-plugin.png?branch=master)](https://travis-ci.org/unix-junkie/teamcity-autotools-plugin)
[![codecov.io](http://codecov.io/github/unix-junkie/teamcity-autotools-plugin/coverage.svg?branch=master)](http://codecov.io/github/unix-junkie/teamcity-autotools-plugin?branch=master)

![Build runner UI](https://github.com/unix-junkie/teamcity-autotools-plugin/wiki/images/teamcity-autotools-ui.png)

See the [Wiki](https://github.com/unix-junkie/teamcity-autotools-plugin/wiki)
section for more screenshots.

### Features

* Build step auto-detection (based on `configure.ac`, `configure.in` or
  `configure` files present in the source tree).
* Build problem reporting.
* Unit test reporting (incl. [DejaGnu](https://www.gnu.org/software/dejagnu/)
  and [Test Anything Protocol](http://testanything.org/)).
* Automatic artifact publishing.
  * If the configure phase fails, `config.log` will be published.
  * If the build phase fails, the generated `config.h` along with the tree of
  `Makefile`'s will be published.
  * If the build succeeds, `make install` will be run (with the appropriate
  `$(DESTDIR)`), and the contents of the install directory will be published as
  a `.tar.gz` archive.
