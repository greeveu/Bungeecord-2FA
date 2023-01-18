# BungeeCord / Waterfall 2FA

This plugin allows anyone two add 2FA Authentication to your BungeeCord / Waterfall network. It utilizes either TOTPs or a YubiKey OTP. For the first one, you just need any app that supports TOTP and for the YubiKey OTP you have to own a YubiKey that has OTP support.

### Setup
To set up, the plugin, just add it to your plugins folder and start the network once. After that, a configuration is generated. Open it and edit it to your liking.
Please note that you have to add MySQL / MariaDB credentials for it to work.
Optional, you can also enable YubiKey OTP support. For that, you will need an API Key for the YubicoCloud, which is free to get. Just visit this Link: https://upgrade.yubico.com/getapikey/

When everything is set, just start back up the Proxy and you can start using the plugin.

### Permissions
`2fa.use` required to use 2FA in the first place  
`2fa.forceenable` to require a person to set up 2FA. (It will force them to set it up the first time they log in)  

### Commands
`/2fa enable` Starts the process for enabling 2FA, it will show you a Code and QR Code which both can be used to add the account to any TOTP App.  
`/2fa activate <OTP>` This command activates 2FA, it requires the OTP to prevent activation without saving the secret.  
`/2fa disable` Disables 2FA  
`/2fa logout` Logs you out from your «session» and requires a login to continue doing things  
`/2fa yubikey add <Name> <OTP>` Adds a new YubiKey to your account. (Requires that 2FA is already activated with TOTP)  
`/2fa yubikey remove <Name>` Removes the YubiKey from your Account  
`/2fa yubikey list` Lists all active YubiKeys added to your account  
