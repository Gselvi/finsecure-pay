import smtplib, getpass
s = smtplib.SMTP("smtp.gmail.com", 587); s.starttls()
s.login("selvi.govind88@gmail.com", getpass.getpass("App password: "))
print("LOGIN OK")
