SELECT UserName FROM Account WHERE UserName LIKE '%User';
SELECT EMail, Telephone FROM AccountProfile WHERE Visible = 1;
SELECT Account.UserName, OnlineState, EMail, ImageLink 
	FROM Account JOIN AccountProfile ON (Account.UserName = AccountProfile.UserName)
	WHERE Visible = 0;
SELECT * FROM ContactRequest WHERE Sender = 'ThirdUser' OR Receiver = 'ThirdUser';
SELECT * FROM Message WHERE MessageTimeStamp = TIMESTAMP('2014-10-14 12:00:00');
SELECT * FROM Message WHERE WasRead = 0;
SELECT Message, MessageTimeStamp FROM Message, SendingGroup
	WHERE Receiver = 'TestGroup' AND Message.MessageID = SendingGroup.MessageID
    ORDER BY MessageTimeStamp ASC;
SELECT Message, Sender, Receiver FROM Message, SendingSingle
	WHERE WasRead = 1 AND Message.MessageID = SendingSingle.MessageID
    ORDER BY MessageTimeStamp DESC;