# Overview

This project is where I experiment building cljs widgets mostly with
reagent and bootstrap css. I usually write a post about lessons
learned building each of these widgets on my blog at
[http://www.upgradingdave.com/blog](http://www.upgradingdave.com/blog).

To run Devcards in hot reloaded dev mode, use `boot dev`, then browse
to [http://localhost:3000/dev.html](http://localhost:3000/dev.html).

To produces a stand alone, advanced compiled `devcards.js` file (that
can be hosted in production), `boot devcards`, then browse to
`http://localhost:3000/devcards.html`

## Clojure Bingo!

For development, run `boot bingo` and then browse to
[http://localhost:3000/bingo-dev.html](http://localhost:3000/bingo-dev.html).
You can also see the application in dev mode at
[http://localhost:3000/bingo.html](http://localhost:3000/bingo.html)

(TODO)
To compile the app for production, run `boot bingo` and then browse to
[http://localhost:3000/bingo-prod.html](http://localhost:3000/bingo-prod.html)

- DONE! cell

- DONE! Basic board

- DONE! Able to Mark Board cells

- DONE! User Sessions
AWS User
u: bingo
Access Key Id:     AKIAJQHQL4AXM5YR3WBA
Secret Access Key: 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

- DONE! Save state of board to db

- DONE! Basic Error Handling
  :error key is populated whenever there's an error

- DONE! Able to see other User's Boards

- DONE! Able to make read only boards

- DONE! Able to see n most recent boards

- DONE! Live polling

- DONE! Implement main screen for multiple devices

- Scoring
Only get words from last minute
Only display buttons for other peoples pending words
Every long poll, check current board for matches on confirmed words
When confirmed, update score.

- Implement winning

- Implement "free" space

- Handle offline

- Implement Invitations

- Make the cells flip

- Use "retro" css styles
https://www.google.com/search?q=retro+bingo&espv=2&biw=1430&bih=689&tbm=isch&source=lnms&sa=X&ved=0ahUKEwjYibmk7LXQAhVF7CYKHUWOBEgQ_AUIBigB#tbm=isch&q=bingo+card&imgrc=9xe5V1jUmkuN3M%3A

- Show notice about cookies

- implement sign in
auth0 iam role
arn:aws:iam::772097437621:role/auth0

bingo-dynamodb policy
arn:aws:iam::772097437621:policy/bingo-dynamodb


