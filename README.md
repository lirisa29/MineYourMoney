# Mine Your Money
OPSC7311 POE

---

## Link to Demo Video
https://youtu.be/cPMT4JVpI0Q
---

## Features Implemented  

### User Authentication  
- Users can log in using a username and password.  
- Data displayed in the app is filtered based on the logged-in user, ensuring that only the relevant wallets and expenses are visible to that specific account for privacy protection.

### Wallets (Categories)  
- Users can create wallets that act as categories for managing expenses.  
- Each wallet has a set balance, which decreases as expenses are added.  

### Expense Management  
- Users can create expense entries by specifying a date, recurrance, and wallet (category).  
- Users have the option to attach a photographor or add a note to each expense entry.  
- All expense data is stored locally using RoomDB and online using Firebase for cross-device syncing.
  
### Budget and Goals  
- Users can set a monthly spending goal to track and control their total expenditure.
- The expenses users log gets deducted from their budget.

### Data Visualization  
- The total expenditure for each wallet is displayed using a line graph.  
- The colour of each line corresponds to the colour of the respective wallet icon.   
- This visualization method allows users to understand their spending distribution at a glance.
- The user can also monitor how well they're staying within their budgeting range using the progress ring graph on the budgeting screen.
- The ring turns blue when below min limit, green when in range and red when over max limit.

### Data Persistence  
- The app uses RoomDB for local data storage and firestore for online data storage.  
- The database contains four main tables:  
  - **User Table** – stores login credentials and user information.  
  - **Wallet Table** – stores details of all wallets created by the user.  
  - **Expense Table** – stores details of all expenses linked to the respective wallets and user accounts.
  - **Budgets Table** - stores details of the monthly budget limit set by the user.  
- Data is relationally linked and filtered by the logged-in user to maintain data separation and privacy.

### Gamification 

---

## Design Decisions  
The project replaces the concept of traditional categories with wallets to give users a more practical and interactive budgeting experience. Each wallet operates like a financial account with a set balance, ensuring users are more conscious of their spending habits.  

A pie chart was chosen for expenditure visualization because it provides a simple and effective overview of spending proportions, making it easy for users to interpret their financial habits quickly.

---

## Development and Version Control  
The project was developed collaboratively using GitHub for version control. GitHub Actions were implemented to protect the main branch from unstable code. Branch protection rules were set to ensure that a merge into the main branch could not occur unless all automated checks passed successfully. This process maintained the stability and integrity of the project throughout development.

---

## Known Bugs and Limitations  
- Expenses cannot currently be edited once created.  
- Users are required to log in each time they open the app, as no persistent login feature is implemented.  
- There is no functionality to update account information such as username or password.  

---

## Future Improvements  
- Implement functionality to edit existing expenses.  
- Add notifications for recurring expenses, such as alerts 24 hours before a scheduled transaction.  
- Introduce a “Stay Logged In” feature for returning users.  
- Add a settings section that allows users to:  
  - Update their account details (username and password)  
  - Delete their account if desired  
