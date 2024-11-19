NGO App

This is a mobile application built for an NGO using Kotlin in Android Studio. The app allows users to view information about the organization, browse events, view team members, and make donations (money or items). Admin users can manage events, team members, and donation items dynamically.

Features

User Features

	•	Donate:
	•	Donate money or items to support the NGO.
	•	View a list of items required by the NGO (e.g., food, clothes, school supplies).
	•	Events:
	•	Browse upcoming and past events, with details and locations.
	•	About Us:
	•	Learn about the organization’s mission, vision, and story.
	•	Meet the Team:
	•	View profiles of the team members.

Admin Features

	•	Donation Management: Add, update, or remove items from the required donations list.
	•	Event Management: Create, update, and delete events.
	•	Team Management: Add, edit, or remove team members.
	•	About Us Update: Edit the organization’s description dynamically.

Technology Stack

Frontend

	•	Kotlin: Android development using modern Kotlin practices (e.g., coroutines, Jetpack libraries).
Backend

	•	Firebase:
	•	Realtime Database for managing data like events, team members, and donation items.

Development Tools

	•	Android Studio: IDE for Android development.
	•	Gradle: Build system for managing dependencies and compiling the app.

Prerequisites

	•	Android Studio (latest version recommended)
	•	A Firebase project set up with Realtime Database

Installation

	1.	Clone the Repository

git clone https://github.com/Group-6-WIL/Mobile-Application  


	2.	Open in Android Studio
	•	Open the project folder in Android Studio.
	3.	Configure Firebase
	•	Create a Firebase project at Firebase Console.
	•	Add the google-services.json file to the app/ directory.
	4.	Run the App
	•	Connect your Android device or use an emulator.
	•	Click Run in Android Studio to install the app on your device.

App Structure

ngo-android-app/  
├── app/  
│   ├── src/  
│   │   ├── main/  
│   │   │   ├── java/com/ngo/  
│   │   │   │   ├── activities/       # Activities for screens  
│   │   │   │   ├── models/           # Data models for Firebase integration  
│   │   │   │   ├── adapters/         # Adapters for RecyclerViews  
│   │   │   │   ├── viewmodels/       # ViewModels for MVVM architecture  
│   │   │   ├── res/                  # Layouts, drawables, strings, etc.  
├── google-services.json             # Firebase configuration  
└── README.md                        # Project documentation  

 

Features Breakdown

User Interface

	1.	Home Screen:
	•	Overview of events and donation items.
	2.	Donate Screen:
	•	Choose to donate money or items.
	•	View a list of items needed by the NGO.
	3.	Events Screen:
	•	Browse upcoming and past events.
	4.	About Us Screen:
	•	Displays mission, vision, and contact information.
	5.	Team Screen:
	•	Profiles of team members.

Admin Interface

	2.	Donation Management:
	•	Add, edit, or delete donation items.
	3.	Event Management:
	•	Manage event details dynamically.
	4.	Team Management:
	•	Add, edit, or remove team members.

Future Enhancements

	•	Add push notifications for new events or donation requests.
	•	Enable photo uploads for donation items.
	•	Integrate payment gateways for monetary donations.
	•	Add a dashboard to track donation progress.

License

This project is licensed under the MIT License.
