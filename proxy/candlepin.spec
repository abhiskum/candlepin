%global _binary_filedigest_algorithm 1
%global _source_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name: candlepin
Summary: Candlepin is an open source entitlement management system.
Group: Internet/Applications
License: GLPv2
Version: 0.0.5
Release: 1
URL: http://fedorahosted.org/candlepin
# Source0: https://fedorahosted.org/releases/c/a/candlepin/%{name}-%{version}.tar.gz
Source: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
Vendor: Red Hat, Inc
BuildArch: noarch

Requires: candlepin-webapp
BuildRequires: java >= 0:1.6.0
#BuildRequires: rubygem-buildr
%define __jar_repack %{nil}

%description
Candlepin is an open source entitlement management system.

%package tomcat5
Summary: Candlepin web application for tomcat5
Requires: tomcat5 >= 5.5
Provides: candlepin-webapp

%description tomcat5
Candlepin web application for tomcat5

%package tomcat6
Summary: Candlepin web application for tomcat6
Requires: tomcat6
Provides: candlepin-webapp

%description tomcat6
Candlepin web application for tomcat6

%package jboss
Summary: Candlepin web application for jboss
Requires: jbossas >= 4.3
Provides: candlepin-webapp

%description jboss
Candlepin web application for jboss

%prep
%setup -q 

%build
buildr clean test=no package

%install
rm -rf $RPM_BUILD_ROOT
# Create the directory structure required to lay down our files
# common
install -d -m 755 $RPM_BUILD_ROOT/%{_sysconfdir}/%{name}/
touch $RPM_BUILD_ROOT/%{_sysconfdir}/%{name}/%{name}.conf

# tomcat5
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/%{name}/
unzip target/%{name}-%{version}.war -d $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat5/webapps/%{name}/

# tomcat6
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/%{name}/
unzip target/%{name}-%{version}.war -d $RPM_BUILD_ROOT/%{_localstatedir}/lib/tomcat6/webapps/%{name}/

# jbossas
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/jbossas/server/production/deploy/
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/jbossas/server/production/deploy/%{name}.war
unzip target/%{name}-%{version}.war -d $RPM_BUILD_ROOT/%{_localstatedir}/lib/jbossas/server/production/deploy/%{name}.war/

%clean
rm -rf $RPM_BUILD_ROOT

%files
%config(noreplace) %{_sysconfdir}/%{name}/%{name}.conf

%files jboss
%defattr(-,jboss,jboss,-)
%{_localstatedir}/lib/jbossas/server/production/deploy/%{name}*

%files tomcat5
%defattr(644,tomcat,tomcat,775)
%{_localstatedir}/lib/tomcat5/webapps/%{name}*

%files tomcat6
%defattr(644,tomcat,tomcat,775)
%{_localstatedir}/lib/tomcat6/webapps/%{name}*

%changelog
* Thu Apr 29 2010 jesus m. rodriguez <jesusr@redhat.com> 0.0.5-1
- 582223 - don't allow the same product to be consumed more than once (jbowes@redhat.com)
- More serial BigInteger/Long fixes. (dgoodwin@redhat.com)
- Revert to using BigInteger for certificate serials. (dgoodwin@redhat.com)
- Allow roles to be specified in candlepin.conf. (dgoodwin@redhat.com)
- Move the client bits into their own directory (bkearney@redhat.com)
- getEntitlementCertificateSerials was returning a entCert's id instead of it's
  serial number (alikins@redhat.com)
- added tests around content set extensions in entitlement certificate.
  (ddolguik@redhat.com)
- Adding a catch clause for CandlepinExceptions, which have localized messages
  (calfonso@redhat.com)
- Remove stray print from cucumber tests (jbowes@redhat.com)
- Move exceptions to their own package, and add them to the api
  (jbowes@redhat.com)
- First cut of writing out the entitlement certificates (bkearney@redhat.com)
- Add additional setters to the certificate class so that resteasy will de-
  serialize it correctly (bkearney@redhat.com)
- added create_pool and create_product methods. added PoolResource#createPool()
  (ddolguik@redhat.com)
- buildfile with a different set of versions, for building against jpackage
  repos. (ayoung@redhat.com)
- No longer ignores versions. Minor cleanup (ayoung@redhat.com)
- Generate unique cert serials from a db sequence. (dgoodwin@redhat.com)
- added create_product method (ddolguik@redhat.com)
- Adding crud methods for owners and consumer_types to ruby lib.  (jharris@redhat.com)
- i18nize AuthInterceptor (jbowes@redhat.com)
- Rename SSLAuthFilterTest -> SSLAuthTest (jbowes@redhat.com)
- Basic list commands work from the command line (bkearney@redhat.com)
- Adding in the product name for poolCurator .find() and .findAll() (jharris@redhat.com)
- Initial client code (bkearney@redhat.com)
- 579913 - clearing out old manager files, adding productname to pool (jharris@redhat.com)
- Replace servlet filter authentication with resteasy interceptors (jbowes@redhat.com)
- Switch from constructor injection to method injection for principal (jbowes@redhat.com)
- Add fuzzy product matching for subscriptions/pools. (dgoodwin@redhat.com)
- Fix up the failing loggingfiltertests (jbowes@redhat.com)
- implmented ConsumerResource#unbindBySerial() (ddolguik@redhat.com)
- Add status output to the logging filter (bkearney@redhat.com)
- Script to generate a sql script to import a product cert as a Product (alikins@redhat.com)
- Add a "type" to the content model (aka, 'yum', 'file').  (alikins@redhat.com)
- Silence ruby warning about argument parenthesis (jbowes@redhat.com)
- chop lambda usage out of cucumber tests (jbowes@redhat.com)
- added retrieval of entitlements by certificate serial number. (ddolguik@redhat.com)
- Fix an NPE with 404s and no media types. (bkearney@redhat.com)
- Add sub/product adapter methods for matching product provides. (dgoodwin@redhat.com)
- move over to use OIDUtil for contept->number mapping instead of hardcoding it (alikins@redhat.com)
- Do not import the sat cert thats full of bogus product info (alikins@redhat.com)
- Need a id seq here (alikins@redhat.com)
- add hash to product and content models (alikins@redhat.com)
- Fix up product/content model mappings. (alikins@redhat.com)
- more roughing out the content info (alikins@redhat.com)
- Add getter/setters for arch/variant/version (alikins@redhat.com)
- Start adding stuff to the product model for the oid product model
- Bundle OIDUtil for now (alikins@redhat.com)
- Change deploy and gen-cert scripts to use the hostname of the machine instead
  of localhost (alikins@redhat.com)

* Fri Apr 23 2010 jesus m. rodriguez <jesusr@redhat.com> 0.0.4-1
- logging cleanup (jbowes@redhat.com)
- Remove untranslated de strings (jbowes@redhat.com)
- lot's of code cleanup, remove dead code, add unit tests (jesusr@redhat.com)
- Protecting all consumer-related methods with @EnforceConsumer (jharris@redhat.com)
- don't allow a duplicate satellite cert to be uploaded (jbowes@redhat.com)
- Search for version specific jar.  If that is mising, go for version agnostic. (ayoung@redhat.com)
- adding a localbuild.rb script, to allow us to fetch only from a local maven repo. (ayoung@redhat.com)
- move localized_resources to po, which is the more standard dir name (jbowes@redhat.com)
- Allow all cucumber tasks to run with specified features (jbowes@redhat.com)
- fix ALL emma reports (jesusr@redhat.com)
- change satellite certificate test to look for 'at least' x products (for multiple runs) (jbowes@redhat.com)
- Convert entitlement certificate tests to cucumber (jbowes@redhat.com)
- remove :fixeclipse hack. Extended the eclipse task instead.  (jesusr@redhat.com)
- fix the emma reports without requiring seperate task. (jesusr@redhat.com)
- Convert subscription token tests to cucumber (jbowes@redhat.com)
- Fix some tests. (dgoodwin@redhat.com)
- Associate uploaded subscription certificates with owners.  (dgoodwin@redhat.com)
- Add setters for the getXXAsString methods so that deserialization works (bkearney@redhat.com)
- Log out the response which is sent to the client (bkearney@redhat.com)
- fixed two broken tests (ddolguik@redhat.com)
- localized jsenforcer error messages (ddolguik@redhat.com)
- updated exceptions thrown from Resources to be serializable to JSON/XML; use
  localized messages (ddolguik@redhat.com)
- remove old java functional tests, use cucumber (jesusr@redhat.com)
- Convert subscription tests to cucumber (jbowes@redhat.com)
- fix whitespace around = (jesusr@redhat.com)
- Rename Certificate to SubscriptionCertificate. (dgoodwin@redhat.com)
- checkstyle needs to depend on compile to get access to Exception info (jesusr@redhat.com)
- fix checkstyle static final NOT final static (jesusr@redhat.com)
- Adding consumer interceptor for validating consumer resources (jharris@redhat.com)
- Remove duplicate nosetest (jbowes@redhat.com)
- allow register by uuid cucumber tests to run without cleaning the db first (jbowes@redhat.com)
- Drop the static encoded cert in CertificateResource. (dgoodwin@redhat.com)
- Convert nosetests to cucumber tests (jbowes@redhat.com)

* Thu Apr 15 2010 jesus m. rodriguez <jesusr@redhat.com> 0.0.3-1
- Change the default SSLProtocol we deploy server.xml with to "all" instead of
  "TLS" (alikins@redhat.com)
- Identity certs should be deleted by their id, not by consumer id (jbowes@redhat.com)
- added support for localization of user-visible messages (ddolguik@redhat.com)
- teach the deploy script to get war version from the spec file (jbowes@redhat.com)
- support for parsing of accept-language headers (ddolguik@redhat.com)

* Wed Apr 14 2010 jesus m. rodriguez <jesusr@redhat.com> 0.0.2-1
- Add support for log4j settings in the candlepin.conf file (bkearney@redhat.com)
- remove BuildRequires altogether (jesusr@redhat.com)
- A bit more easy emma love (bkearney@redhat.com)
- Remove the test resource (jbowes@redhat.com)
- remove changelog, bump down version to 0.0.0 (jesusr@redhat.com)
- Fixing up checkstyle  and cucumber test.  (jharris@redhat.com)
