cd C:\CapstoneProject\finsecure-pay
@'
import smtplib, getpass
s = smtplib.SMTP("smtp.gmail.com", 587); s.starttls()
s.login("selvi.govind88@gmail.com", getpass.getpass("App password: "))
print("LOGIN OK")
'@ | Set-Content -Encoding utf8 test_smtp.py
python test_smtp.py