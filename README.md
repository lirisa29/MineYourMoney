# Mine Your Money
OPSC7311 POE

---

## Link to Demo Video

---

## Features Implemented  

### User Authentication  
- Users can log in using a username and password.  
- Data displayed in the app is filtered based on the logged-in user, ensuring that only the relevant wallets and expenses are visible to that specific account for privacy protection.

### Wallets (Categories)  
- Users can create wallets that act as categories for managing expenses.  
- Each wallet has a set balance, which decreases as expenses are added.  
- Users are only allowed to add an expense if the wallet’s balance exceeds the expense amount. This system encourages users to be more aware of their spending habits.

### Expense Management  
- Users can create expense entries by specifying a date, recurrance, and wallet (category).  
- Users have the option to attach a photographor or add a note to each expense entry.  
- All expense data is stored locally using RoomDB and linked to the specific user account.

### Budget and Goals  
- Users can set a monthly spending goal to track and control their total expenditure.

### Data Visualization  
- The total expenditure for each wallet is displayed using a pie chart.  
- The colour of each pie segment corresponds to the colour of the selected wallet icon.  
- When a segment is clicked, a toast message appears showing the wallet name and the total amount spent.  
- This visualization method allows users to understand their spending distribution at a glance.

### Data Persistence  
- The app uses RoomDB for local data storage and management.  
- The database contains three main tables:  
  - **User Table** – stores login credentials and user information.  
  - **Wallet Table** – stores details of all wallets created by the user.  
  - **Expense Table** – stores details of all expenses linked to the respective wallets and user accounts.  
- Data is relationally linked and filtered by the logged-in user to maintain data separation and privacy.

---

## Design Decisions  
The project replaces the concept of traditional categories with wallets to give users a more practical and interactive budgeting experience. Each wallet operates like a financial account with a set balance, ensuring users are more conscious of their spending habits.  

A pie chart was chosen for expenditure visualization because it provides a simple and effective overview of spending proportions, making it easy for users to interpret their financial habits quickly.

---

## Development and Version Control  
The project was developed collaboratively using GitHub for version control. GitHub Actions were implemented to protect the main branch from unstable code. Branch protection rules were set to ensure that a merge into the main branch could not occur unless all automated checks passed successfully. This process maintained the stability and integrity of the project throughout development.

---

## Known Bugs and Limitations  
- Wallets and expenses cannot currently be edited once created.  
- Users are required to log in each time they open the app, as no persistent login feature is implemented.  
- There is no functionality to update account information such as username or password.  

---

## Future Improvements  
- Implement functionality to edit existing wallets and expenses.  
- Add notifications for recurring expenses, such as alerts 24 hours before a scheduled transaction.  
- Introduce a “Stay Logged In” feature for returning users.  
- Add a settings section that allows users to:  
  - Update their account details (username and password)  
  - Delete their account if desired  
