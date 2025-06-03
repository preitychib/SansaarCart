# SansarCart

A robust, multi-vendor e-commerce backend built with Spring Boot, Spring Security, Hibernate/JPA, and MySQL. SansarCart manages the complete lifecycle of multi-vendor commerce—from user onboarding and role-based access control, hierarchical category and dynamic product variation management, to cart/wishlist handling, consolidated order placement, and an order state transition workflow.

---

### Project Resources & Links
- [Database Diagram and Flows (Excalidraw)](https://excalidraw.com/#json=Xz_FbnsrdcXIQZnC_aU_h,fLSlNcEm0yRTEpg57bvu6A)
- [Design Documentation (Google Docs)](https://docs.google.com/document/d/1kALUvPOaH8J_vTFYPddSfKzSqfNeBFLzWDcQ5a3ftXE/edit?usp=sharing)
- [Pitch and Idea Presentation](https://docs.google.com/presentation/d/1yuafQ5v3wZSpDOkZoSj0vp_ZkiazzVbH/edit?usp=sharing&ouid=117660264163683601698&rtpof=true&sd=true)

---

## 1. End-to-End System Flow

```
                      +---------------------------------------+
                      |       Bootstrap / System Admin        |
                      +---------------------------------------+
                                          |
                      +-------------------+-------------------+
                      |                                       |
                      v                                       v
          +-----------------------+               +-----------------------+
          |  Seller Registration  |               | Customer Registration |
          +-----------------------+               +-----------------------+
                      |                                       |
           Awaits Admin Approval                   Email Activation Token (3h)
                      |                                       |
                      v                                       v
          +-----------------------+               +-----------------------+
          | Active Seller Account |               | Active Customer Acct  |
          +-----------------------+               +-----------------------+
                      |                                       |
         +------------+------------+                          |
         |                         |                          |
         v                         v                          v
+------------------+     +-------------------+    +-----------------------+
| Manage Category  |     | Add Products &    |    | Browse Leaf Categories|
| Metadata Fields  |     | Variations (JSON) |    | & Product Variations  |
+------------------+     +-------------------+    +-----------------------+
                                   |                          |
                                   v                          v
                        +--------------------+    +-----------------------+
                        | Live Catalog (INR) |<---| Cart / Wishlist Items |
                        +--------------------+    +-----------------------+
                                                              |
                                                    Place Order (Consolidated)
                                                              |
                                                              v
                                                  +-----------------------+
                                                  | ORDER_PLACED Status   |
                                                  +-----------------------+
                                                              |
                                      +-----------------------+-----------------------+
                                      |                                               |
                                      v                                               v
                          +-----------------------+                       +-----------------------+
                          | Seller Confirms Order |                       | Customer Cancels      |
                          +-----------------------+                       +-----------------------+
                                      |                                               |
                                      v                                               v
                          +-----------------------+                       +-----------------------+
                          | Shipped -> Delivered  |                       | Refund / Closed       |
                          +-----------------------+                       +-----------------------+
                                      |
                                      v
                          +-----------------------+
                          | Return / Refund Flow  |
                          +-----------------------+
```

### High-Level Lifecycle:
1. **Onboarding & Activation**:
   - **Customers** register and receive an asynchronous email with an activation token (valid for 3 hours). The account activates upon verification.
   - **Sellers** register with their business profile, GST, and single official address. Seller accounts must be reviewed and activated by an **Admin**.
   - An **Admin** account is initialized via bootstrapping and cannot be deleted or deactivated.
2. **Catalog & Variation Setup**:
   - Admins establish hierarchical categories and attach dynamic metadata fields (e.g., Size, Color).
   - Sellers create products assigned strictly to **leaf categories**, defining product-level details and one or more product variations with flat JSON metadata, quantities, INR pricing, and primary images.
3. **Discovery & Cart Flow**:
   - Customers explore products, filtering by categories and metadata attributes.
   - Customers move items between **Cart** (stock-checked) and **Wishlist** (allowed even if out of stock).
4. **Order Fulfillment & State Machine**:
   - On checkout, a consolidated `ORDER` is created with address snapshot details and individual `ORDER_PRODUCT` items.
   - Sellers accept/reject and advance order status (shipped, delivered).
   - Customers can cancel orders prior to delivery or request returns once delivered.
   - A midnight scheduler (00:00) alerts sellers of pending orders that require action.

---

## 2. User Roles & Capabilities

### Common User Operations
- **Registration**: Sign up with email, password, and personal details.
- **Authentication**: JWT token-based login and secure logout.
- **Password Management**: Request a password reset email (token valid for 15 minutes) and set a new password complying with security criteria.

### Admin
- **Profile Management**: Update name, profile image, and password.
- **User Moderation**: List registered customers and sellers; activate, deactivate, or modify user roles.
- **Catalog Oversight**: List all products across the system; activate or deactivate products.
- **Order Administration**: Monitor orders across the platform and update order item statuses when required.

### Seller
- **Profile Management**: Update company profile, contact details, and password. Limited strictly to a single associated business address.
- **Catalog Management**: Add new products, update existing listings, delete products, and define variation-specific attributes.
- **Fulfillment**: Review customer orders for their own products, confirm or reject new orders, and update shipping and delivery states.

### Customer
- **Profile & Address Book**: Update personal info, avatar, and password; manage multiple saved delivery addresses (e.g., Home, Office).
- **Product Discovery**: Browse active products by category and seller; view comprehensive product pages including variations, metadata, images, pricing, and seller details.
- **Cart & Wishlist**: Add products to cart (quantity-validated) or wishlist; seamlessly move items between cart and wishlist.
- **Orders & Tracking**: Place orders from cart; view full order history with status tracking.
- **Post-Purchase Actions**: Cancel placed orders (before delivery); submit return requests for delivered items; leave product ratings and reviews.

---

## 3. Core Business & Domain Rules

### Category & Product Model
- **Category Hierarchy**: Multi-level category tree. Products are linked only to **leaf categories** (categories without subcategories).
- **Dynamic Metadata**:
  - `CATEGORY_METADATA_FIELD` defines attributes (e.g., `Size`, `Color`).
  - `CATEGORY_METADATA_FIELD_VALUES` binds allowed values to categories (e.g., `Shirt: M, L, XL`).
- **Product Variations**:
  - Each variation stores attributes as a flat JSON structure matching category metadata fields.
  - Variation-level metadata overrides product-level metadata.
  - Variations specify individual price, stock quantity, and primary display image.
  - Product price is standardized in **INR**.
  - When listing products, all active variation primary images are exposed.

### Cart & Wishlist Logic
- **Cart**: Enforces stock validation. A product variation can be added to the cart only if `quantity_available > 0`.
- **Wishlist**: Allows saving items regardless of availability (can add out-of-stock items).
- Items can be shifted bidirectionally between cart and wishlist.

### Order State Machine
Orders follow a strict transition model tracked at the line-item level (`ORDER_PRODUCT`):

| Current Status (`fromStatus`) | Permitted Next Status (`toStatus`) |
| :--- | :--- |
| `ORDER_PLACED` | `CANCELLED`, `ORDER_CONFIRMED`, `ORDER_REJECTED` |
| `ORDER_CONFIRMED` | `CANCELLED`, `ORDER_SHIPPED` |
| `ORDER_REJECTED` | `REFUND_INITIATED`, `CLOSED` |
| `CANCELLED` | `REFUND_INITIATED`, `CLOSED` |
| `ORDER_SHIPPED` | `DELIVERED` |
| `DELIVERED` | `RETURN_REQUESTED`, `CLOSED` |
| `RETURN_REQUESTED` | `RETURN_REJECTED`, `RETURN_APPROVED` |
| `RETURN_REJECTED` | `CLOSED` |
| `RETURN_APPROVED` | `PICK_UP_INITIATED` |
| `PICK_UP_INITIATED` | `PICK_UP_COMPLETED` |
| `PICK_UP_COMPLETED` | `REFUND_INITIATED` |
| `REFUND_INITIATED` | `REFUND_COMPLETED` |
| `REFUND_COMPLETED` | `CLOSED` |

> **Key Constraints**:
> - An order item can only be cancelled **before** it has been delivered.
> - Return and refund requests can only be made **after** the item has been marked `DELIVERED`.

---

## 4. Technical Specifications & Standards

### Security & Tokens
- **Password Policy**: 8 to 15 characters, containing at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character. Stored exclusively via cryptographic hashing (BCrypt).
- **Access Token (`access_token`)**: Configurable JWT sent in the `Authorization` header for protected endpoints. Accompanied by a `refresh_token` valid for 24 hours.
- **Activation Token (`activation_token`)**: Time-bound token (valid for 3 hours) sent via email upon customer registration.
- **Password Reset Token (`reset_password_token`)**: Time-bound token (valid for 15 minutes) for account recovery.
- **Account Protection**: Fields for `is_locked`, `is_expired`, `invalid_attempt_count`, and `password_update_date` to defend against brute-force attempts.

### Asynchronous Operations & Scheduling
- **Asynchronous Mail**: All outbound emails (activations, password resets, notifications) execute asynchronously using Spring `@Async` to prevent blocking HTTP threads.
- **Daily Reminder Scheduler**: Runs every day at midnight (`00:00`) to detect orders pending seller confirmation/rejection and dispatch reminder emails.

### Image Storage Architecture
- **Computed Path Storage**: Files are saved in a local filesystem directory (`/upload` or `<BASE_PATH>`) rather than storing full file URLs in the database:
  - Users: `<BASE_PATH>/users/<user_id>.<ext>`
  - Products: `<BASE_PATH>/products/<product_id>/<product_id>.<ext>`
  - Variations: `<BASE_PATH>/products/<product_id>/variations/<variation_id>.<ext>`
- **Supported Formats**: `jpg`, `jpeg`, `png`, `bmp`.

### API & Database Conventions
- **HTTP Response Codes**:
  - `200 OK` / `201 Created`: Successful operations.
  - `401 Unauthorized`: Unauthenticated request or expired token.
  - `403 Forbidden`: Authenticated user lacks required role/permissions.
  - `404 Not Found`: Resource URL or entity ID not found.
  - `405 Method Not Allowed`: Incorrect HTTP method used.
  - `500 Internal Server Error`: Unhandled application or service exceptions.
- **Pagination & Sorting Defaults**:
  - Default Page Size (`max`): `10`
  - Default Offset (`offset`): `0`
  - Default Sort Field: Entity `id`, `date_created`, or `email`
  - Default Order: `ASC`
  - Query Filter: Key-value pair matching.
- **Persistence & Auditing**:
  - Inheritance: `@Inheritance(strategy = InheritanceType.JOINED)` for `User`, `Customer`, and `Seller`.
  - Soft Deletes: Entities use `is_deleted` flag to retain history.
  - Auditing: Common fields `date_created`, `last_updated`, `created_by`, `updated_by` via `AuditInfo`.
- **Internationalization (i18n)**: Locale-based messaging managed through `message.properties` bundles.

---

## 5. Database Schema & Relations

### Entity Relationship Summary

| Relationship | Entities | Mapping Type | Ownership & Notes |
| :--- | :--- | :--- | :--- |
| **User - Role** | `USER` - `ROLE` | Many-to-Many | Owned by `USER` via `USER_ROLE`. Scalable role assignment. |
| **User - Address** | `USER` - `ADDRESS` | One-to-Many | Owned by `ADDRESS`. Customer has multiple; Seller has one. |
| **Category - Category** | `CATEGORY` - `CATEGORY` | Many-to-One | Self-referencing via `parent_category_id`. |
| **Product - Category** | `PRODUCT` - `CATEGORY` | Many-to-One | Unidirectional; references leaf category only. |
| **Product - Variation** | `PRODUCT` - `PRODUCT_VARIATION`| One-to-Many | Bidirectional; parent product manages variations. |
| **Product - Review** | `PRODUCT` - `PRODUCT_REVIEW` | One-to-Many | Unidirectional/Bidirectional review association. |
| **Category Metadata** | `CATEGORY` - `METADATA_FIELD` | Many-to-Many | Managed through `CATEGORY_METADATA_FIELD_VALUES` with composite key. |
| **Cart - Variation** | `CART` - `PRODUCT_VARIATION` | Many-to-One | Owned by `CART` (`is_wishlist_item` distinguishes cart vs wishlist). |
| **Order - OrderProduct**| `ORDER` - `ORDER_PRODUCT` | One-to-Many | `ORDER` holds consolidated billing & address snapshot; `ORDER_PRODUCT` holds items. |
| **Order Item Status** | `ORDER_PRODUCT` - `ORDER_STATUS`| One-to-Many | Tracks status history and transition notes. |

### Core Schema Tables

```
USER                            ROLE                  USER_ROLE
- id (UUID / Long)              - id (Long)           - user_id (FK)
- email                         - authority           - role_id (FK)
- password (encoded)
- first_name, middle_name, last_name
- is_active, is_deleted, is_locked, is_expired
- invalid_attempt_count, password_update_date

SELLER (extends USER)           CUSTOMER (extends USER)     ADDRESS
- user_id (FK)                  - user_id (FK)              - id
- gst                           - contact                   - user_id (FK)
- company_name                                              - address_line, city, state
- company_contact                                           - country, zip_code, label

CATEGORY                        CATEGORY_METADATA_FIELD     CATEGORY_METADATA_FIELD_VALUES
- id                            - id                        - category_id (FK)
- name                          - name                      - category_metadata_field_id (FK)
- parent_category_id (FK)                                   - values (comma-separated string)

PRODUCT                         PRODUCT_VARIATION           PRODUCT_REVIEW
- id                            - id                        - id
- seller_user_id (FK)           - product_id (FK)           - product_id (FK)
- category_id (FK, leaf)        - price (INR)               - customer_user_id (FK)
- name, brand, description      - quantity_available        - rating, review
- is_active, is_deleted         - metadata (JSON)
- is_cancellable                - primary_image_name
- is_returnable                 - is_active

CART                            ORDER                       ORDER_PRODUCT
- customer_user_id (FK)         - id                        - id
- product_variation_id (FK)     - customer_user_id (FK)     - order_id (FK)
- quantity                      - amount_paid, date_created - product_variation_id (FK)
- is_wishlist_item (boolean)    - payment_method            - quantity, price
                                - customer_address_*
                                  (city, state, line, etc.) ORDER_STATUS
                                                            - id, order_product_id (FK)
                                                            - from_status, to_status
                                                            - transition_notes_comments
                                                            - transition_date
```
