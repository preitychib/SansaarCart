## Contains all the whys?

1. check if single table strategy is: 
   - @Inheritance(strategy = InheritanceType.JOINED) vs (strategy = InheritanceType.SINGLE_TABLE)
   - We have kept JOINED as in future field in customer and seller may increase or we can have another type of user
   - a lot of join happening when i retrieve user and customer and everything 
  
2. check if sequence is better for role table. Where to use uuid
3. Address is One to Many: additional field isSellerAddress field is removed as it was no longer needed
4. Audit 4 filed be embedded or not and how to handle what
    - Spring has its own auditing may include that later phases
   
5. **User Role Relation**: User Role relation is kept Many to Many for scalability. We can have more roles in future that can be assign to same user. 

6. UUID for user table for scalability and security?

7. Indexing of id_deleted for better results after multiple soft deletion

8. Why order table has all the address listed even though customer has addresses:
   - we have flexibility to add new address that user dont have
   - we can have a temporary user address that will not be saved

9. Why cart table is not user individual cart but cart items or all user cart items of a product variation

10. Similar products: similar product will be on basis of the leaf category associated with the product, its description?

11. Initially jaccard similarity strategy but found dice better (these are simple string matching method use in basic nlp problems)


###  E-Commerce Backend Utilities (Suggested)
- **Common Util File**
- **Date and Time**
- **Object Mapper for DTO**
- **Bean Copy Utils**
- **Date for Timestamp**
- **Seller-Product - Bidirectional?**
- **Leave Category**
- **Custom Advice**
- **ToString Implementation**
- **Order mein Embedded**
- **Date Filters and Other Filters**
- **Mapper**
- **Folder DAO where needed**
- **DTO should go in Response**
- **Have Test Data in DB**
- **Swagger and Actuator**
- **Object Function toString in Logger**




 