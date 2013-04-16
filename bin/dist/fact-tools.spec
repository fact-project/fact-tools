Name:		fact-tools
Version:	%_version
Release:	%_revision
Summary:	The fact-tools are a collection of processors and stream implementations for processing FACT telescope data with the streams Framework
Group:		admin
License:	AGPL
URL:		http://www.jwall.org/streams/fact-tools/
Source0:	http://www.jwall.org/download/jwall-tools/jwall-tools-latest-src.zip
BuildRoot:	/Volumes/RamDisk/facttools/fact-tools
Requires:	streams >= 0.9.5


Group: Applications/System


%description
Brief description of software package.

%prep

%build

%install

%clean

%files -f ../rpmfiles.list
%defattr(-,root,root)
%doc

%changelog
