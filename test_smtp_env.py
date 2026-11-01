import smtplib, os
s = smtplib.SMTP("smtp.gmail.com", 587); s.starttls()
s.login("selvi.govind88@gmail.com", os.environ["SMTP_PW"])
print("LOGIN OK")
