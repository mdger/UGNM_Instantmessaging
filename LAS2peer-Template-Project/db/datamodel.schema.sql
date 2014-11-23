CREATE TABLE IF NOT EXISTS AccountProfile (
	UserName VARCHAR(20) NOT NULL,
    EMail VARCHAR(50)
		CHECK (EMail = '%@%.%'),
	Telephone INT,
    ImageLink VARCHAR(500),
    NickName VARCHAR(20),
    Visible TINYINT(1),
    
    PRIMARY KEY (UserName)
);

CREATE TABLE IF NOT EXISTS Message (
	MessageID INT NOT NULL AUTO_INCREMENT,
    Message VARCHAR(500) NOT NULL,
    MessageTimeStamp DATETIME NOT NULL,
    WasRead TINYINT(1) NOT NULL,
    
    PRIMARY KEY (MessageID)
);

CREATE TABLE IF NOT EXISTS Groups (
	GroupName VARCHAR(20) NOT NULL,
    FounderName VARCHAR(20) NOT NULL,
    Description VARCHAR(200),
    ImageLink VARCHAR (500),
    
    PRIMARY KEY (GroupName),
    FOREIGN KEY (FounderName) REFERENCES AccountProfile(UserName)
);

CREATE TABLE IF NOT EXISTS Contact (
	ContactID INT NOT NULL AUTO_INCREMENT,
    FirstUser VARCHAR(20) NOT NULL,
    SecondUser VARCHAR(20) NOT NULL,
    
    PRIMARY KEY (ContactID),
    FOREIGN KEY (FirstUser) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (SecondUser) REFERENCES AccountProfile(UserName)
);

CREATE TABLE IF NOT EXISTS ContactRequest (
	RequestID INT NOT NULL AUTO_INCREMENT,
    Sender VARCHAR(20) NOT NULL,
    Receiver VARCHAR(20) NOT NULL,
    
    PRIMARY KEY (RequestID),
    FOREIGN KEY (Sender) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (Receiver) REFERENCES AccountProfile(UserName)
);

CREATE TABLE IF NOT EXISTS SendingSingle (
	SingleID INT NOT NULL AUTO_INCREMENT,
    Sender VARCHAR(20) NOT NULL,
    Receiver VARCHAR(20) NOT NULL,
    MessageID INT NOT NULL,
    
    PRIMARY KEY (SingleID),
    FOREIGN KEY (Sender) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (Receiver) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (MessageID) REFERENCES Message(MessageID)
);

CREATE TABLE IF NOT EXISTS SendingGroup (
	GroupID INT NOT NULL AUTO_INCREMENT,
    Sender VARCHAR(20) NOT NULL,
    Receiver VARCHAR(20) NOT NULL,
    MessageID INT NOT NULL,
    
    PRIMARY KEY (GroupID),
    FOREIGN KEY (Sender) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (Receiver) REFERENCES Groups(GroupName),
    FOREIGN KEY (MessageID) REFERENCES Message(MessageID)
);

CREATE TABLE IF NOT EXISTS MemberOf (
	MemberID INT NOT NULL AUTO_INCREMENT,
    UserName VARCHAR(20) NOT NULL,
    GroupName VARCHAR(20) NOT NULL,
    
    PRIMARY KEY (MemberID),
    FOREIGN KEY (UserName) REFERENCES AccountProfile(UserName),
    FOREIGN KEY (GroupName) REFERENCES Groups(GroupName)
);