# Admin Activities API Documentation

This document describes the Admin Activities API endpoints that have been implemented to track and manage admin activities in the Volunteer Skills Bank application.

## Overview

The Admin Activities API provides comprehensive tracking of administrative actions, including user management, content moderation, organization approvals, and system activities. All activities are logged with timestamps, user information, and metadata for audit purposes.

## Base URL

All endpoints are prefixed with `/api/admin/activities`

## Authentication

All endpoints require admin authentication. Include the admin session token in the request headers.

## Endpoints

### 1. Get Recent Activities

**GET** `/api/admin/activities/recent`

Fetches recent activities for the admin dashboard with pagination.

**Query Parameters:**
- `limit` (optional, default: 10) - Number of activities to fetch
- `offset` (optional, default: 0) - Number of activities to skip

**Response:**
```json
[
  {
    "activityId": "activity_123",
    "type": "user_registration",
    "action": "New volunteer registration",
    "userId": "user_456",
    "userName": "John Doe",
    "adminId": "admin_789",
    "adminName": "Admin User",
    "details": "John Doe completed their volunteer registration",
    "timestamp": "2024-01-15T10:30:00",
    "timeAgo": "2 hours ago",
    "metadata": {
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0..."
    }
  }
]
```

### 2. Get Activities by Type

**GET** `/api/admin/activities/type`

Fetches activities filtered by a specific type.

**Query Parameters:**
- `type` (required) - Activity type to filter by
- `limit` (optional, default: 10) - Number of activities to fetch

**Available Activity Types:**
- `user_registration` - New volunteer registrations
- `post_approval` - Content approvals
- `post_rejection` - Content rejections
- `post_deletion` - Content deletions by admins
- `organization_approval` - Organization verifications
- `organization_rejection` - Organization request rejections
- `user_suspension` - User account suspensions
- `user_reactivation` - User account reactivations
- `group_creation` - Volunteer group creations
- `skill_addition` - New skill additions
- `report_resolved` - Report resolutions

**Response:** Same as recent activities endpoint

### 3. Get User Activities

**GET** `/api/admin/activities/user/{userId}`

Fetches all activities for a specific user.

**Path Parameters:**
- `userId` (required) - ID of the user

**Query Parameters:**
- `limit` (optional, default: 10) - Number of activities to fetch

**Response:** Same as recent activities endpoint

### 4. Log Activity

**POST** `/api/admin/activities/log`

Logs a new admin activity.

**Request Body:**
```json
{
  "type": "user_registration",
  "action": "New volunteer registration",
  "userId": "user_456",
  "userName": "John Doe",
  "adminId": "admin_789",
  "adminName": "Admin User",
  "details": "John Doe completed their volunteer registration",
  "metadata": {
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0..."
  }
}
```

**Response:**
```json
{
  "activityId": "activity_123",
  "type": "user_registration",
  "action": "New volunteer registration",
  "userId": "user_456",
  "userName": "John Doe",
  "adminId": "admin_789",
  "adminName": "Admin User",
  "details": "John Doe completed their volunteer registration",
  "timestamp": "2024-01-15T10:30:00",
  "timeAgo": "just now",
  "metadata": {
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0..."
  }
}
```

### 5. Get Filtered Activities

**GET** `/api/admin/activities/filtered`

Fetches activities with advanced filtering and pagination.

**Query Parameters:**
- `type` (optional) - Activity type filter
- `userId` (optional) - User ID filter
- `adminId` (optional) - Admin ID filter
- `dateFrom` (optional) - Start date filter (ISO 8601 format)
- `dateTo` (optional) - End date filter (ISO 8601 format)
- `limit` (optional, default: 10) - Number of activities to fetch
- `offset` (optional, default: 0) - Number of activities to skip

**Response:**
```json
{
  "activities": [
    {
      "activityId": "activity_123",
      "type": "user_registration",
      "action": "New volunteer registration",
      "userId": "user_456",
      "userName": "John Doe",
      "adminId": "admin_789",
      "adminName": "Admin User",
      "details": "John Doe completed their volunteer registration",
      "timestamp": "2024-01-15T10:30:00",
      "timeAgo": "2 hours ago",
      "metadata": {
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0..."
      }
    }
  ],
  "total": 150,
  "hasMore": true,
  "limit": 10,
  "offset": 0
}
```

### 6. Get Activity Statistics

**GET** `/api/admin/activities/stats`

Fetches activity statistics for the dashboard.

**Response:**
```json
{
  "totalActivities": 1250,
  "todayActivities": 23,
  "weeklyActivities": 156,
  "monthlyActivities": 567,
  "activityTypes": {
    "user_registration": 45,
    "post_approval": 32,
    "post_rejection": 8,
    "organization_approval": 12,
    "user_suspension": 3,
    "group_creation": 18,
    "skill_addition": 7,
    "report_resolved": 5
  }
}
```

## Activity Types Reference

| Type | Description | Action |
|------|-------------|---------|
| `user_registration` | New volunteer registrations | "New volunteer registration" |
| `post_approval` | Content approvals | "Post approved" |
| `post_rejection` | Content rejections | "Post rejected" |
| `post_deletion` | Content deletions by admins | "Post deleted" |
| `organization_approval` | Organization verifications | "Organization approved" |
| `organization_rejection` | Organization request rejections | "Organization rejected" |
| `user_suspension` | User account suspensions | "User suspended" |
| `user_reactivation` | User account reactivations | "User reactivated" |
| `group_creation` | Volunteer group creations | "Group created" |
| `skill_addition` | New skill additions | "New skill added" |
| `report_resolved` | Report resolutions | "Report resolved" |

## Usage Examples

### Logging Activities in Your Code

Use the `ActivityLogger` utility class to log activities throughout your application:

```java
@Autowired
private ActivityLogger activityLogger;

// Log user registration
activityLogger.logUserRegistration(userId, userName, adminId, adminName);

// Log post approval
activityLogger.logPostApproval(userId, userName, adminId, adminName, postTitle);

// Log custom activity
Map<String, Object> metadata = new HashMap<>();
metadata.put("ipAddress", request.getRemoteAddr());
metadata.put("userAgent", request.getHeader("User-Agent"));

activityLogger.logCustomActivity(
    "custom_action",
    "Custom action performed",
    userId,
    userName,
    adminId,
    adminName,
    "Detailed description of the action",
    metadata
);
```

### Frontend Integration

The API endpoints match the frontend service structure provided:

```javascript
import { activitiesApi } from './services/activitiesApi';

// Get recent activities
const activities = await activitiesApi.getRecentActivities(10, 0);

// Get activities by type
const userRegistrations = await activitiesApi.getActivitiesByType('user_registration', 20);

// Get filtered activities
const filtered = await activitiesApi.getFilteredActivities({
  type: 'post_approval',
  dateFrom: '2024-01-01T00:00:00',
  dateTo: '2024-01-31T23:59:59',
  limit: 50
});

// Get statistics
const stats = await activitiesApi.getActivityStats();

// Log new activity
const newActivity = await activitiesApi.logActivity({
  type: 'user_registration',
  action: 'New volunteer registration',
  userId: 'user_123',
  userName: 'John Doe',
  adminId: 'admin_456',
  adminName: 'Admin User',
  details: 'John Doe completed their volunteer registration'
});
```

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK` - Successful request
- `201 Created` - Activity successfully logged
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `500 Internal Server Error` - Server error

## Database Schema

Activities are stored in the `admin_activities` Firestore collection with the following structure:

```json
{
  "activityId": "string (auto-generated)",
  "type": "string",
  "action": "string",
  "userId": "string",
  "userName": "string",
  "adminId": "string",
  "adminName": "string",
  "details": "string",
  "timestamp": "Timestamp",
  "metadata": "Map<String, Object>"
}
```

## Performance Considerations

- Activities are sorted by timestamp in descending order (newest first)
- Pagination is implemented to handle large datasets
- Repository methods are optimized for common query patterns
- Statistics are calculated efficiently using Firestore aggregation queries

## Security

- All endpoints require admin authentication
- Activity logging includes IP address and user agent for audit trails
- Sensitive information should not be included in activity details
- Admin actions are logged for compliance and accountability
