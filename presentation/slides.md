---
theme: default
class: text-center
highlighter: shikiji
lineNumbers: true
drawings:
  persist: false
transition: slide-left
title: Cloud Project Presentation
mdc: true
---

# AWS Testing with Localstack

---

## Demo Web App

Why: A user wants to be notified when a new product is released by his favorite shops

What: Scalable Web App based on REST APIs

How: with AWS Services

---

## Entities

There are 3 types of users:

<v-clicks>

- Customer - A generic user
- Shop - It releases new products
- Admin - Can turn a generic user account into a Shop account

</v-clicks>

---

## First requirement: Authentication & Authorization
