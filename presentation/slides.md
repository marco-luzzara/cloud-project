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
layout: center
---

## Workflow #1: Shop Creation

<img src="/shop_creation.jpg" class="h-100 rounded shadow" />

---
layout: center
---

## Workflow #2: User Registration & Login

<img src="/user_signup_login.jpg" class="h-100 rounded shadow" />

---
layout: center
---

## Workflow #3: New Product Release

<img src="/pub_sub.jpg" class="h-100 rounded shadow" />

---

## Data Layer

Where to store Users and Shops info?

<img src="/db_diagram.jpg" class="h-35 rounded shadow" />

A relational database is suitable for the two tables and a single relationship

Solution with AWS? **RDS** (Relational Database Service)

- ✅ Consistency
- ✅ Easy scalability (by sharding on user id & shop owner id)
- ❌ What about subscriptions?

---

## User Subscriptions

*A user wants to be notified **WHEN** a new product is released by his favorite shops*

Requirements:

- Event-based system <mingcute-arrow-right-fill /> Every product released is an event  
- Pub/Sub system <mingcute-arrow-right-fill /> Events are selectively delivered

Solution with AWS? **SNS** (Simple Notification Service)

- ✅ publishers are not aware of subscribers, the broker takes care of that (no need for us to store them)
- ✅ SNS notifications can be sent through emails (or processed by Lambda Functions for more complex scenarios)




