# radio-recorder
This is a desktop Java program that can be imported as an Eclipse project. It can extract data from online sound or radio streams and store it into local files. Then it will upload them to your Dropbox account.

# Steps to use it.
1. Create a Dropbox account for yourself.
2. Inside your Dropbox account go to create app.
3. Set a specific folder for your app, then give it permissions to read and write files. Then generate an access token.
4. You'll have to pass that access token to the program as an argument every time you run it so that it knows where to upload the files.
5. You'll have to create a local folder in your computer and set it in the corresponding placeholder in the program so it knows where to save the local files.
6. You'll have to set in the program a valid streaming URL.
