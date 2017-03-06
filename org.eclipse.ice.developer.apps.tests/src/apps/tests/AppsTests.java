/**
 */
package apps.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

/**
 * <!-- begin-user-doc -->
 * A test suite for the '<em><b>apps</b></em>' package.
 * <!-- end-user-doc -->
 * @generated
 */
public class AppsTests extends TestSuite {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static Test suite() {
		TestSuite suite = new AppsTests("apps Tests");
		suite.addTestSuite(EnvironmentManagerTest.class);
		suite.addTestSuite(JsonEnvironmentCreatorTest.class);
		suite.addTestSuite(EnvironmentConsoleTest.class);
		return suite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AppsTests(String name) {
		super(name);
	}

} //AppsTests
