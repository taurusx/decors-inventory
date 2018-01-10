# DecorsInventory App by [taurusx](https://taurusx.github.io/)

App preview:

- Start by adding decors to your database:

![DecorsInventory Empty][screenshot-1] ![DecorsInventory List][screenshot-2]

- Edit decor anytime, quickly change quantity:

![DecorsInventory Edit][screenshot-3] ![DecorsInventory Discard][screenshot-4] ![DecorsInventory Saved][screenshot-5]

- Set photo from a gallery as a decor image:

![DecorsInventory Browse Permission][screenshot-6] ![DecorsInventory Gallery][screenshot-7] ![DecorsInventory Decor's Image][screenshot-8]

- Add supplier's email to send orders with ease:

![DecorsInventory Email][screenshot-9] ![DecorsInventory Email Intent][screenshot-10] 


## General Description

Done as a part of [Android Basics Udacity's course][udacity-course]

Last app in a course is about working with databases. DecorsInventory App stores information about an item, in particular the decor that florists use as a part of their flower deocrations (vases, crates, candle holders etc.). It displays all available items in a form of list where every item can easily be *'sold'*, which means the quantity is reduced by 1. You can add images to easily recognize your decor. You can send order to your supplier when there is a need to get more decors. 

## Main Goals

**Main Goals** of the task:
1. Main layout contains `ListView` populated by custom `CursorAdapter`.
2. The app utilizes custom database helper class (`SQLiteOpenHelper`) and `ContentProvider` to access, query and edit database
3. Contract (Entry) static class is set to keep relevant `URI`s and database-related information (e.g. column names)
4. `LoaderManager` and `CursorLoader` is used for performing database operations when the database list is being loaded
5. Edit mode is used to add new item or edit existing ones from database
6. Buttons to quickly change available decor's quantity in edit mode
7. Use the implicit `Intent`s to pass supplier's email to mailing app; subject of the email is set to be in form of: 'Order' + decor's name
8. Implement *'SALE'* button which works on a specific item in a database and reduces quantity by 1
9. `FloatingActionButton` starts new activity in which you can add new item to a database

## Related Work

Previous apps: [BookSearch][book-search], [NewsFeed][news-feed].

[udacity-course]: https://eu.udacity.com/course/android-basics-nanodegree-by-google--nd803
[screenshot-1]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-1.png
[screenshot-2]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-2.png
[screenshot-3]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-3.png
[screenshot-4]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-4.png
[screenshot-5]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-5.png
[screenshot-6]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-6.png
[screenshot-7]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-7.png
[screenshot-8]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-8.png
[screenshot-9]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-9.png
[screenshot-10]: https://raw.githubusercontent.com/taurusx/decors-inventory/gh-pages/assets/images/decors-inventory-screenshot-10.png
[news-feed]: https://github.com/taurusx/news-feed
[book-search]: https://github.com/taurusx/book-search

