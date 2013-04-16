VERSION=0.3.1
REVISION=1
STREAMS_VERSION=0.9.8-SNAPSHOT
MAVEN=mvn
SKIP_TESTS=true
DIST=jwall
JARFILE=fact-tools.jar
NAME=fact-tools
ARCH=noarch
DEB_FILE=${NAME}-${VERSION}-${REVISION}.deb
RPM_FILE=${NAME}-${VERSION}-${REVISION}.${ARCH}.rpm
BUILD=.build
RPMBUILD=.rpmbuild
RELEASE_DIR=releases

MAVEN_FLAGS=-DskipTests=true -Dstreams.version=${STREAMS_VERSION}

all:
	@echo "Building $(JARFILE)..."
	@echo " + Compile output will be redirected to $(PWD)/.build.log"
	@mvn -DskipTests=$(SKIP_TESTS) ${MAVEN_FLAGS} package > $(PWD)/.build.log 
	@echo " + Compilation successful."
	@echo " + Creating $(JARFILE)"
	@cp target/$(JARFILE) .
	@echo "Fact tools built."
	@echo ""
	@echo "To start the fact-tools run"
	@echo ""
	@echo "     java -jar fact-tools.jar your-xml-file.xml"
	@echo ""

clean:
	rm -rf ${BUILD}
	mvn clean

pre-package:
	echo "Preparing packages build in ${BUILD}"
	mkdir -p ${BUILD}
	mkdir -p ${BUILD}/opt/streams/plugins
	rm -rf target/dependency/*
	mvn -Dbinary.name=${NAME}-${VERSION} ${MAVEN_FLAGS} -Dstreams.scope=provided clean package
	cp target/${NAME}-${VERSION}.jar ${BUILD}/opt/streams/plugins/
#	mvn -DskipTests=true ${MAVEN_FLAGS} -Dstreams.scope=provided -DexcludeArtifactIds=junit,hamcrest-core -DexcludeScope=provided dependency:copy-dependencies
#	cp target/dependency/*.jar ${BUILD}/opt/streams/plugins/


deb:    pre-package
	rm -rf ${RELEASE_DIR}
	mkdir -p ${RELEASE_DIR}
	mkdir -p ${BUILD}/DEBIAN
	cp dist/DEBIAN/* ${BUILD}/DEBIAN/
	cat dist/DEBIAN/control | sed -e 's/Version:.*/Version: ${VERSION}-${REVISION}/' > ${BUILD}/DEBIAN/control
	chmod 755 ${BUILD}/DEBIAN/p*
	cd ${BUILD} && find opt -type f -exec md5sum {} \; > DEBIAN/md5sums && cd ..
	dpkg -b ${BUILD} ${RELEASE_DIR}/${DEB_FILE}
	md5sum ${RELEASE_DIR}/${DEB_FILE} > ${RELEASE_DIR}/${DEB_FILE}.md5
	rm -rf ${BUILD}
	debsigs --sign=origin --default-key=C5C3953C ${RELEASE_DIR}/${DEB_FILE}

release-deb:
	reprepro --ask-passphrase -b /var/www/download.jwall.org/htdocs/debian includedeb ${DIST} ${RELEASE_DIR}/${DEB_FILE}


unrelease-deb:
	reprepro --ask-passphrase -b /var/www/download.jwall.org/htdocs/debian remove ${DIST} streams


rpm:
	mkdir -p ${RELEASE_DIR}
	mkdir -p ${RPMBUILD}
	mkdir -p ${RPMBUILD}/tmp
	mkdir -p ${RPMBUILD}/RPMS
	mkdir -p ${RPMBUILD}/RPMS/${ARCH}
	mkdir -p ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/SRPMS
	rm -rf ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/SPECS
	cp -a dist/fact-tools.spec ${RPMBUILD}/SPECS
	mkdir -p ${RPMBUILD}/BUILD/opt/streams/plugins/
	rm -rf target/dependency/*
	mvn -Dbinary.name=${NAME}-${VERSION} ${MAVEN_FLAGS} -Dstreams.scope=provided clean package
	cp target/${NAME}-${VERSION}.jar ${RPMBUILD}/BUILD/opt/streams/plugins/
	find .rpmbuild/BUILD -type f | sed -e s/^\.rpmbuild\\/BUILD// | grep -v DEBIAN > ${RPMBUILD}/rpmfiles.list
	rpmbuild --target noarch --sign --define '_topdir ${RPMBUILD}' --define '_version ${VERSION}' --define '_revision ${REVISION}' -bb ${RPMBUILD}/SPECS/fact-tools.spec --buildroot ${RPMBUILD}/BUILD/
	cp ${RPMBUILD}/RPMS/${ARCH}/${RPM_FILE} ${RELEASE_DIR}
	md5sum ${RELEASE_DIR}/${RPM_FILE} > ${RELEASE_DIR}/${RPM_FILE}.md5

release-rpm:
	mkdir -p /var/www/download.jwall.org/htdocs/yum/${DIST}/noarch
	cp ${RELEASE_DIR}/${RPM_FILE} /var/www/download.jwall.org/htdocs/yum/${DIST}/noarch/
	createrepo /var/www/download.jwall.org/htdocs/yum/${DIST}/
