package com.preeti.sansarcart.service;

import com.preeti.sansarcart.entity.Product;
import com.preeti.sansarcart.payload.email.EmailDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// i18n and timezone update
@Service
public class EmailBuilderService {

    @Value("${app.admin.username}")
    private static String adminEmail;

    @Value("${app.base-url}")
    private static String baseUrl;

    static public EmailDetails buildActivationTokenEmail(String to, String token) {
        String subject = "Activate Your SansarCart Account";
        String activationLink = "https://sansarcart.com/activate?token=" + token;

        String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <div style="background-color: #f5f5f5; padding: 20px;">
                        <h2 style="color: #333;">Welcome to SansarCart!</h2>
                        <p style="font-size: 16px;">Please click the button below to activate your account:</p>
                        <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                                    box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center;">
                            <a href="%s" style="background-color: #007bff; color: white; padding: 10px 20px;
                                    text-decoration: none; border-radius: 5px; font-size: 16px;">
                                Activate Account
                            </a>
                            <p style="font-size: 14px; color: #666; margin-top: 10px;">
                                This link is valid for 3 hours.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(activationLink);

        return new EmailDetails(to, subject, htmlBody);
    }

    static public EmailDetails buildPasswordResetEmail(String to, String token) {
        String subject = "Reset Your SansarCart Password";
        String resetLink = "https://sansarcart.com/reset-password?token=" + token;

        String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <div style="background-color: #f5f5f5; padding: 20px;">
                        <h2 style="color: #333;">Password Reset Request</h2>
                        <p style="font-size: 16px;">Click the button below to reset your password:</p>
                        <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                                    box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center;">
                            <a href="%s" style="background-color: #dc3545; color: white; padding: 10px 20px;
                                    text-decoration: none; border-radius: 5px; font-size: 16px;">
                                Reset Password
                            </a>
                            <p style="font-size: 14px; color: #666; margin-top: 10px;">
                                This link is valid for 15 minutes.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetLink);

        return new EmailDetails(to, subject, htmlBody);
    }

    static public EmailDetails buildPasswordUpdatedEmail(String to) {
        String subject = "Your SansarCart Password Has Been Updated";

        String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="background-color: #f5f5f5; padding: 20px;">
                    <h2 style="color: #28a745;">Password Updated Successfully</h2>
                    <p style="font-size: 16px;">Your SansarCart account password has been successfully changed.</p>
                    <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                                box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                        <p style="font-size: 14px; color: #666;">
                            If you did not initiate this change, please contact our support immediately.
                        </p>
                        <p style="font-size: 14px; color: #666;">
                            <strong>Time:</strong> %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        return new EmailDetails(to, subject, htmlBody);
    }

    static public EmailDetails buildSellerRegistrationEmail(String to, String sellerName) {
        String subject = "Welcome to SansarCart - Seller Registration Received";

        String htmlBody = """
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px;
                        border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50;">Hello %s,</h2>
                <p style="font-size: 16px; color: #333;">
                    Thank you for registering as a seller on <strong>SansarCart</strong>!
                </p>
                <p style="font-size: 16px; color: #333;">
                    Your seller account has been created and is currently <strong>pending approval</strong> by our team.
                </p>
                <p style="font-size: 16px; color: #333;">
                    Once approved, you’ll be notified and can start listing your products immediately.
                </p>
                <p style="font-size: 16px; color: #555;">
                    If you have any questions, feel free to reach out to our support team.
                </p>
                <p style="font-size: 14px; color: #999; margin-top: 30px;">
                    Regards,<br/>
                    Team SansarCart
                </p>
            </div>
        </body>
        </html>
    """.formatted(sellerName);

        return new EmailDetails(to, subject, htmlBody);
    }


    static public EmailDetails buildAccountLockedEmail(String to) {
        String subject = "Your SansarCart Account Has Been Locked";

        String htmlBody = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <div style="background-color: #f5f5f5; padding: 20px;">
                <h2 style="color: #dc3545;">Account Locked</h2>
                <p style="font-size: 16px;">
                    Your SansarCart account has been locked due to multiple unsuccessful login attempts.
                </p>
                <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                            box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <p style="font-size: 14px; color: #666;">
                        For your security, we’ve temporarily disabled access to your account.
                        It will be automatically unlocked after <strong>1 hour</strong>.
                    </p>
                    <p style="font-size: 14px; color: #666;">
                        <strong>Time:</strong> %s
                    </p>
                    <p style="font-size: 14px; color: #666;">
                        If this wasn't you, please contact our support team immediately.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));

        return new EmailDetails(to, subject, htmlBody);
    }

    public static EmailDetails buildAdminActivationEmail(String to) {
        String subject = "Your SansarCart Account Has Been Activated";

        String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="background-color: #f5f5f5; padding: 20px;">
                    <h2 style="color: #333;">Account Activated</h2>
                    <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                                box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center;">
                        <p style="font-size: 16px; color: #333;">
                            Your SansarCart account has been successfully activated by administrator.
                        </p>
                        <p style="font-size: 14px; color: #666; margin-top: 10px;">
                            You can now sign in and start using your account.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return new EmailDetails(to, subject, htmlBody);
    }

    public static EmailDetails buildAdminDeactivationEmail(String to) {
        String subject = "Your SansarCart Account Has Been Deactivated";

        String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="background-color: #f5f5f5; padding: 20px;">
                    <h2 style="color: #333;">Account Deactivated</h2>
                        <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                            box-shadow: 0 0 10px rgba(0,0,0,0.1); text-align: center;">
                            <p style="font-size: 16px; color: #333;">
                            Your SansarCart account has been deactivated by the administrator.
                            </p>
                            <p style="font-size: 14px; color: #666; margin-top: 10px;">
                            If you believe this was a mistake or need assistance, please contact support.
                            </p>
                    </div>
                </div>
            </body>
            </html>
            """;

        return new EmailDetails(to, subject, htmlBody);
    }


    public static EmailDetails buildProductActivationEmailToAdmin(Product product) {
            String subject = "New Product Awaiting Activation - SansarCart";
            String activationDashboardLink = baseUrl + "/admin/products/" + product.getId();

            String htmlBody = """
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="background-color: #f5f5f5; padding: 20px;">
                    <h2 style="color: #333;">New Product Submitted for Approval</h2>
                    <p style="font-size: 16px;">A seller has added a new product that requires your approval:</p>
                    <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                                box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                        <p><strong>Product Name:</strong> %s</p>
                        <p><strong>Brand:</strong> %s</p>
                        <p><strong>Category:</strong> %s</p>
                        <p><strong>Seller:</strong> %s</p>
                        <p><strong>Description:</strong> %s</p>
                        <div style="margin-top: 20px; text-align: center;">
                            <a href="%s" style="background-color: #28a745; color: white; padding: 10px 20px;
                                    text-decoration: none; border-radius: 5px; font-size: 16px;">
                                Review Product
                            </a>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                    product.getProductName(),
                    product.getBrand(),
                    product.getCategory().getCategoryName(),
                    product.getSeller().getCompanyName(),
                    product.getDescription() != null ? product.getDescription() : "N/A",
                    activationDashboardLink
            );

            return new EmailDetails(adminEmail, subject, htmlBody);
    }

    public static EmailDetails buildProductActivatedEmailToSeller(Product product) {
        String subject = "Your Product Has Been Activated - SansarCart";

        String htmlBody = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <div style="background-color: #f5f5f5; padding: 20px;">
                <h2 style="color: #28a745;">Product Activated</h2>
                <p style="font-size: 16px;">Great news! Your product has been reviewed and is now active on SansarCart:</p>
                <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                            box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <p><strong>Product Name:</strong> %s</p>
                    <p><strong>Brand:</strong> %s</p>
                    <p><strong>Category:</strong> %s</p>
                </div>
            </div>
        </body>
        </html>
    """.formatted(
                product.getProductName(),
                product.getBrand(),
                product.getCategory().getCategoryName()
        );

        return new EmailDetails(
                product.getSeller().getEmail(),
                subject,
                htmlBody
        );
    }

    public static EmailDetails buildProductDeactivatedEmailToSeller(Product product) {
        String subject = "Your Product Has Been Deactivated - SansarCart";

        String htmlBody = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <div style="background-color: #f5f5f5; padding: 20px;">
                <h2 style="color: #dc3545;">Product Deactivated</h2>
                <p style="font-size: 16px;">Your product has been deactivated by the admin and is no longer visible to customers:</p>
                <div style="background-color: #fff; padding: 20px; border-radius: 5px;
                            box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <p><strong>Product Name:</strong> %s</p>
                    <p><strong>Brand:</strong> %s</p>
                    <p><strong>Category:</strong> %s</p>
                </div>
            </div>
        </body>
        </html>
    """.formatted(
                product.getProductName(),
                product.getBrand(),
                product.getCategory().getCategoryName()
        );

        return new EmailDetails(
                product.getSeller().getEmail(),
                subject,
                htmlBody
        );
    }



}

