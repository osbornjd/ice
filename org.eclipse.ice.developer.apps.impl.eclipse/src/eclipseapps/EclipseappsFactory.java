/**
 */
package eclipseapps;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see eclipseapps.EclipseappsPackage
 * @generated
 */
public interface EclipseappsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	EclipseappsFactory eINSTANCE = eclipseapps.impl.EclipseappsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Eclipse Environment Storage</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Eclipse Environment Storage</em>'.
	 * @generated
	 */
	EclipseEnvironmentStorage createEclipseEnvironmentStorage();

	/**
	 * Returns a new object of class '<em>Docker PTP Sync Project Launcher</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Docker PTP Sync Project Launcher</em>'.
	 * @generated
	 */
	DockerPTPSyncProjectLauncher createDockerPTPSyncProjectLauncher();

	/**
	 * Returns a new object of class '<em>Eclipse Cpp Project Provider</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Eclipse Cpp Project Provider</em>'.
	 * @generated
	 */
	EclipseCppProjectProvider createEclipseCppProjectProvider();

	/**
	 * Returns a new object of class '<em>Eclipse Environment Console</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Eclipse Environment Console</em>'.
	 * @generated
	 */
	EclipseEnvironmentConsole createEclipseEnvironmentConsole();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	EclipseappsPackage getEclipseappsPackage();

} //EclipseappsFactory
