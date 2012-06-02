What is this?
==============

This is an example command-line (CLI) application that uses [fitbit4j][0] to get data from the [Fitbit API][1].

I build it to show the simplest way to get your data from fitbit without all the headaches that come from using [fitbit4j][0].

How do I run this?
==================

[Register a *desktop* application][3] and copy your applications *consumer key* and *secret*.

Make sure you have [Maven][2] installed, then run

    mvn -DCONSUMER_KEY=yourapplicationkey -DCONSUMER_SECRET=yourapplicationsecret exec:java

on your command-line.

How can I build on top of this?
===============================

[Fork my repository][4], open a command-line in the directory and run

    mvn eclipse:eclipse

to create project files for [Eclipse][5]. Start hacking and don't forget to [submit a pull request][6]

Example output
==============

    Open http://www.fitbit.com/oauth/authorize?oauth_token=0dc1323a475f2cd77e9b30c72bc86559
    Enter PIN:INSERTTHECOPIEDPINHERE
    YOURNAME, member since 2000-01-01
    Your sleep on 2012-01-01: inBed=234 asleep=123

[0]: https://github.com/Fitbit/fitbit4j
[1]: http://dev.fitbit.com/
[2]: http://maven.apache.org/ 
[3]: https://dev.fitbit.com/apps
[4]: XXX/fork
[5]: http://eclipse.org/
[6]: XXX/pulls