package com.examsystem.util;

import com.examsystem.network.NetworkManager;
import com.examsystem.rmi.RMIManager;
import com.examsystem.sync.SyncManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs out and returns to the login screen without requiring network connectivity.
 */
public final class AuthLogoutHelper {
    private static final Logger logger = LoggerFactory.getLogger(AuthLogoutHelper.class);

    private AuthLogoutHelper() {
    }

    public static void logoutToLogin(Stage stage) {
        try {
            try {
                SyncManager.getInstance().shutdown();
            } catch (Exception e) {
                logger.debug("Sync shutdown during logout: {}", e.getMessage());
            }
            try {
                NetworkManager.getInstance().disconnectClient();
                NetworkManager.getInstance().stopServer();
            } catch (Exception e) {
                logger.debug("Network shutdown during logout: {}", e.getMessage());
            }
            try {
                RMIManager.getInstance().disconnectClient();
                RMIManager.getInstance().stopServer();
            } catch (Exception e) {
                logger.debug("RMI shutdown during logout: {}", e.getMessage());
            }

            Session.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(
                    AuthLogoutHelper.class.getResource("/com/examsystem/fxml/login.fxml"));
            Parent root = loader.load();
            UiManager.navigateToLogin(stage, root);
        } catch (Exception e) {
            logger.error("Logout failed", e);
            throw new RuntimeException("Could not return to login: " + e.getMessage(), e);
        }
    }
}
