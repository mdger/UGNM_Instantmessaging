INSERT INTO Account (UserName, UserPassword, OnlineState) VALUES ('TestUser', 'Password1', 'Offline');
INSERT INTO Account (UserName, UserPassword, OnlineState) VALUES ('OtherUser', 'Password2', 'Offline');
INSERT INTO Account (UserName, UserPassword, OnlineState) VALUES ('ThirdUser', 'Password3', 'Offline');

INSERT INTO AccountProfile (UserName, EMail, Telephone, ImageLink, NickName, Visible)
	VALUES ('TestUser', 'TestUser@somewhere.de', 12345678, 'www.somewhere.com/image1.jpg', 'Nick1', 1);
INSERT INTO AccountProfile (UserName, EMail, Telephone, ImageLink, NickName, Visible)
	VALUES ('OtherUser', 'OtherUser@somewhere.de', 12345678, 'www.somewhere.com/image2.jpg', 'Nick2', 0);
INSERT INTO AccountProfile (UserName, Visible)
	VALUES ('ThirdUser', 0);
    
INSERT INTO Contact (FirstUser, SecondUser) VALUES ('TestUser', 'OtherUser');

INSERT INTO ContactRequest (Sender, Receiver) VALUES ('TestUser', 'ThirdUser');
INSERT INTO ContactRequest (Sender, Receiver) VALUES ('ThirdUser', 'OtherUser');

INSERT INTO Groups (GroupName, FounderName, Description, ImageLink)
	VALUES ('TestGroup', 'TestUser', 'A group which is created only for test purposes', 'www.somewhere.com/group.jpg');

INSERT INTO MemberOf (UserName, GroupName) VALUES ('OtherUser', 'TestGroup');
INSERT INTO MemberOf (UserName, GroupName) VALUES ('ThirdUser', 'TestGroup');

INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestMessage', TIMESTAMP('2014-01-13 12:00:00'), 0);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTestMessageMessage', TIMESTAMP('2014-10-13 12:00:00'), 1);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTestTestMessageMessageMessage', TIMESTAMP('2014-10-14 12:00:00'), 1);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('Test', TIMESTAMP('2014-01-15 12:00:00'), 0);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTest', TIMESTAMP('2014-01-16 12:00:00'), 1);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTestTest', TIMESTAMP('2014-01-17 12:00:00'), 0);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTestMessage', TIMESTAMP('2014-01-18 12:00:00'), 1);
INSERT INTO Message (Message, MessageTimeStamp, WasRead) VALUES ('TestTestMessageMessageMessage', TIMESTAMP('2014-01-19 12:00:00'), 1);

INSERT INTO SendingGroup (Sender, Receiver, MessageID) VALUES ('TestUser', 'TestGroup', 8);
INSERT INTO SendingGroup (Sender, Receiver, MessageID) VALUES ('OtherUser', 'TestGroup', 1);
INSERT INTO SendingGroup (Sender, Receiver, MessageID) VALUES ('ThirdUser', 'TestGroup', 2);

INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('TestUser', 'OtherUser', 3);
INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('TestUser', 'OtherUser', 4);
INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('OtherUser', 'TestUser', 5);
INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('OtherUser', 'TestUser', 6);
INSERT INTO SendingSingle (Sender, Receiver, MessageID) VALUES ('TestUser', 'OtherUser', 7);