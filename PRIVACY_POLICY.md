# CourseCompass Privacy Policy

Last updated: 2026-02-07

CourseCompass is a Chrome extension that adds an “Analyze” button to Lou’s List pages and (optionally) calls a CourseCompass backend API to generate a short summary for a professor/course.

## What data we collect

CourseCompass itself does **not** require you to create an account.

When you click **Analyze**, the extension sends the following to the CourseCompass backend API:

- **Professor name** (as shown on Lou’s List)
- **Course identifier** (e.g., “CS 3240”)

The extension does **not** intentionally collect:

- Your name, email address, or student ID
- Your browsing history outside of Lou’s List
- Passwords, payment information, or precise location

## How we use the data

The professor name and course identifier are used only to:

- Query public sources (e.g., Reddit discussions and RateMyProfessors) to produce an “analysis” response
- Return the resulting analysis back to you in the page UI
- Optionally cache results server-side to improve performance and reduce repeated requests

## Data retention

If server-side caching is enabled, the backend may store:

- Professor name
- Course identifier
- Derived result fields (e.g., average rating, short summary)
- A timestamp of when the result was last updated

This cache is used to speed up future requests for the same professor/course. Cached entries may be refreshed periodically.

## Third-party services

The backend may fetch public information from third-party websites (for example, Reddit and RateMyProfessors). Their own privacy policies and terms apply.

## Sharing

We do not sell personal data. We do not share personal data with advertisers.

## Security

We take reasonable steps to protect the backend and any cached data. However, no method of transmission or storage is 100% secure.

## Children’s privacy

CourseCompass is not directed to children under 13.

## Changes to this policy

We may update this policy from time to time. The “Last updated” date will reflect the latest version.

## Contact

If you have any questions, contact:

- Email: aryan20544@gmail.com
