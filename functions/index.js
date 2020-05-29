//Dependencies
const functions = require('firebase-functions');
const admin = require('firebase-admin');

//Get Admin privilege from configuration
admin.initializeApp(functions.config().firebase);

//Define function for sending notifications
exports.addMessage = functions.https.onCall((data, res) => {
    //Define notification payload from https call parameters
    const payload = {
		notification: {
			title: data.title,
			body: data.description
		},
		data: {
			owner: data.owner,
		}
    };

    // Send the message with FCM and return the result.
    //Topics are used to handle who receives the notifications
  return admin.messaging().sendToTopic("/topics/" +data.searchParty, payload)
    .then(function (response) {
      console.log('Notification sent successfully for ', data.title, response);
	  return null;
    })
    .catch(function (error) {
      console.log('Notification failed to send for ', data.title, error);
	  return null;
    });
});


