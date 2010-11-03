/*
 * A Mac OS X Jar Bundler Ant Task.
 *
 * Copyright (c) 2003, Seth J. Morabito <sethm@loomcom.com> All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


package net.sourceforge.jarbundler;

// Java Utility
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.io.File;

// Java language imports
import java.lang.String;

public class AppBundleProperties {

	// Required
	private String mApplicationName;
	private String mMainClass;

	// Application short name
	private String mCFBundleName = null;

	// Finder version, with default
	private String mCFBundleShortVersionString = "1.0";

	// Get Info string, optional
	private String mCFBundleGetInfoString = null;

	// Build number, optional
	private String mCFBundleVersion = null;

	// Help Book folder, optional
	private String mCFHelpBookFolder = null;

	// Help Book name, optional
	private String mCFHelpBookName = null;

	// StartOnMainThread, optional
	private Boolean mStartOnMainThread = null;

	// Explicit default: false
	private boolean mCFBundleAllowMixedLocalizations = false;

	// Explicit default: JavaApplicationStub
	private String mCFBundleExecutable = "JavaApplicationStub";

	// Explicit default: English
	private String mCFBundleDevelopmentRegion = "English";

	// Explicit default: APPL
	private final String mCFBundlePackageType = "APPL";

	// Explicit default: ????
	private String mCFBundleSignature = "????";

	// Explicit default: 1.3+
	private String mJVMVersion = "1.3+";

	// Explicit default: 6.0
	private final String mCFBundleInfoDictionaryVersion = "6.0";

	// Optional keys, with no defaults.

	private String mCFBundleIconFile = null;
    private String mSplashFile = null;
	private String mCFBundleIdentifier = null;
	private String mVMOptions = null; // Java VM options
	private String mWorkingDirectory = null; // Java Working Dir
	private String mArguments = null; // Java command line arguments

	// Class path and extra class path elements
	private List mClassPath = new ArrayList();
	private List mExtraClassPath = new ArrayList();

	// Java properties
	private Hashtable mJavaProperties = new Hashtable();

	// Document types
	private List mDocumentTypes = new LinkedList();

	// Services
	private List mServices = new LinkedList();
	
	// ================================================================================

	/**
	 * Add a Java runtime property to the properties hashtable.
	 */

	public void addJavaProperty(String prop, String val) {
		mJavaProperties.put(prop, val);
	}

	public Hashtable getJavaProperties() {
		return mJavaProperties;
	}

	public void addToClassPath(String s) {
		mClassPath.add("$JAVAROOT/" + s.replace(File.separatorChar, '/'));
	}

	public void addToExtraClassPath(String s) {
		mExtraClassPath.add(s);
	}

	public List getExtraClassPath() {
		return mExtraClassPath;
	}

	public DocumentType createDocumentType() {
		return new DocumentType();
	}

	public List getDocumentTypes() {
		return mDocumentTypes;
	}

	/**
	 * Add a document type to the document type list.
	 */
	public void addDocumentType(DocumentType documentType) {
		mDocumentTypes.add(documentType);
	}

	public Service createService() {
		return new Service();
	}
	
	public List getServices() {
		return mServices;
	}
	
	/**
	 * Add a service to the services list.
	 */
	public void addService(Service service) {
		mServices.add(service);
	}
	
	// ================================================================================

	public void setApplicationName(String s) {
		mApplicationName = s;
	}

	public String getApplicationName() {
		return mApplicationName;
	}

	// ================================================================================
	//
	// Bundle setters and getters
	//

	public void setCFBundleName(String s) {

		if (s.length() > 16)
			System.err
					.println("WARNING: 'shortname' is recommeded to be no more than 16 "
							+ "charaters long. See usage notes.");
		mCFBundleName = s;
	}

	public String getCFBundleName() {
		if (mCFBundleName == null)
			return getApplicationName();

		return mCFBundleName;
	}

	public void setCFBundleVersion(String s) {
		mCFBundleVersion = s;
	}

	public String getCFBundleVersion() {
		return mCFBundleVersion;
	}

	public void setCFBundleInfoDictionaryVersion(String s) {
		// mCFBundleInfoDictionaryVersion = s;
	}

	public String getCFBundleInfoDictionaryVersion() {
		return mCFBundleInfoDictionaryVersion;
	}

	public void setCFBundleIdentifier(String s) {
		mCFBundleIdentifier = s;
	}

	public String getCFBundleIdentifier() {
		return mCFBundleIdentifier;
	}

	public void setCFBundleGetInfoString(String s) {
		mCFBundleGetInfoString = s;
	}

	public String getCFBundleGetInfoString() {
		if (mCFBundleGetInfoString == null)
			return getCFBundleShortVersionString();

		return mCFBundleGetInfoString;
	}

	public void setCFBundleShortVersionString(String s) {
		mCFBundleShortVersionString = s;
	}

	public String getCFBundleShortVersionString() {
		return mCFBundleShortVersionString;
	}

	public void setCFBundleIconFile(String s) {
		mCFBundleIconFile = s;
	}

	public String getCFBundleIconFile() {
		return mCFBundleIconFile;
	}

    public void setSplashFile(String s) {
        mSplashFile = s;
    }

    public String getSplashFile() {
        return mSplashFile;
    }

	public void setCFBundleAllowMixedLocalizations(boolean b) {
		mCFBundleAllowMixedLocalizations = b;
	}

	public boolean getCFBundleAllowMixedLocalizations() {
		return mCFBundleAllowMixedLocalizations;
	}

	public void setCFBundleExecutable(String s) {
		mCFBundleExecutable = s;
	}

	public String getCFBundleExecutable() {
		return mCFBundleExecutable;
	}

	public void setCFBundleDevelopmentRegion(String s) {
		mCFBundleDevelopmentRegion = s;
	}

	public String getCFBundleDevelopmentRegion() {
		return mCFBundleDevelopmentRegion;
	}

	public void setCFBundlePackageType(String s) {
		// mCFBundlePackageType = s;
	}

	public String getCFBundlePackageType() {
		return mCFBundlePackageType;
	}

	public void setCFBundleSignature(String s) {
		mCFBundleSignature = s;
	}

	public String getCFBundleSignature() {
		return mCFBundleSignature;
	}

	public void setCFBundleHelpBookFolder(String s) {
		mCFHelpBookFolder = s;
	}

	public String getCFBundleHelpBookFolder() {
		return mCFHelpBookFolder;
	}

	public void setCFBundleHelpBookName(String s) {
		mCFHelpBookName = s;
	}

	public String getCFBundleHelpBookName() {
		return mCFHelpBookName;
	}


	public void setStartOnMainThread(Boolean b) {
		mStartOnMainThread = b;
	}

	public Boolean getStartOnMainThread() {
		return mStartOnMainThread;
	}

	public void setMainClass(String s) {
		mMainClass = s;
	}

	public String getMainClass() {
		return mMainClass;
	}

	public void setJVMVersion(String s) {
		mJVMVersion = s;
	}

	public String getJVMVersion() {
		return mJVMVersion;
	}

	public void setVMOptions(String s) {
		mVMOptions = s;
	}

	public String getVMOptions() {
		return mVMOptions;
	}

	public void setWorkingDirectory(String s) {
		mWorkingDirectory = s;
	}

	public String getWorkingDirectory() {
		return mWorkingDirectory;
	}

	public void setArguments(String s) {
		mArguments = s;
	}

	public String getArguments() {
		return mArguments;
	}

	public List getClassPath() {
		return mClassPath;
	}

}
