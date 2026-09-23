# IT Support Ticket System Product Requirements Document
# Tài liệu yêu cầu sản phẩm Hệ thống Quản lý Vé Hỗ trợ Kỹ thuật

**Document status / Trạng thái:** Draft for review / Dự thảo chờ review  
**Version / Phiên bản:** 1.0  
**Product / Sản phẩm:** IT Support Ticket System  
**Industry / Lĩnh vực:** Internal IT service management / Quản lý dịch vụ CNTT nội bộ  
**Delivery assumption / Giả định triển khai:** One-day MVP / MVP trong một ngày

## MVP Boundary / Ranh giới MVP

This SDD exercise implements one vertical slice only: an employee creates one equipment support ticket, a Tech Lead assigns it to one IT technician, and the technician updates the ticket through resolution.

Bài tập SDD này chỉ triển khai một vertical slice: nhân viên tạo một ticket hỗ trợ thiết bị, Tech Lead phân công cho một nhân viên IT và nhân viên IT cập nhật ticket đến khi xử lý xong.

The slice supports one requester, one technician, one ticket, one equipment item, and the workflow `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED`.

Phạm vi gồm một người yêu cầu, một kỹ thuật viên, một ticket, một thiết bị và workflow `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED`.

The following are outside this MVP: email notifications, attachments, SLA automation, comments, approval workflows, asset inventory, knowledge base, user authentication, dashboards, multiple technicians per ticket, and integration with external service management tools.

Các nội dung sau nằm ngoài MVP: thông báo email, tệp đính kèm, tự động SLA, bình luận, quy trình phê duyệt, quản lý tài sản, knowledge base, xác thực người dùng, dashboard, nhiều kỹ thuật viên cho một ticket và tích hợp công cụ bên ngoài.

Only the requirements marked `MVP-SDD` are implementation commitments. Other requirements are product backlog items for a later increment.

Chỉ các yêu cầu được đánh dấu `MVP-SDD` là cam kết triển khai. Các yêu cầu khác là backlog cho increment sau.

## 1. Purpose / Mục đích

The IT Support Ticket System is a small web API for reporting and handling internal IT problems. It gives employees a clear way to request technical help and gives the IT team a simple workflow for ownership and progress.

Hệ thống Quản lý Vé Hỗ trợ Kỹ thuật là một web API nhỏ để báo cáo và xử lý sự cố CNTT nội bộ. Hệ thống giúp nhân viên gửi yêu cầu hỗ trợ rõ ràng và giúp đội IT quản lý người xử lý cùng tiến độ.

The MVP focuses on one complete ticket lifecycle. It is designed as a small example for Spec-Driven Development, Domain-Driven Design, Harness Engineering, and GitHub Copilot-assisted implementation.

MVP tập trung vào một vòng đời ticket hoàn chỉnh. Đây là ví dụ nhỏ để áp dụng Spec-Driven Development, Domain-Driven Design, Harness Engineering và phát triển với GitHub Copilot.

## 2. Business Goals / Mục tiêu nghiệp vụ

1. Give employees one standard way to report equipment problems.  
   Cung cấp một cách chuẩn để nhân viên báo cáo sự cố thiết bị.
2. Make ticket ownership clear after Tech Lead assignment.  
   Làm rõ người chịu trách nhiệm sau khi Tech Lead phân công.
3. Show the current technical progress of each ticket.  
   Hiển thị tiến độ xử lý kỹ thuật của từng ticket.
4. Prevent invalid status changes and missing assignments.  
   Ngăn chuyển trạng thái không hợp lệ và ticket chưa được phân công.
5. Provide a small, testable example that can be built and demonstrated in one day.  
   Cung cấp ví dụ nhỏ, dễ kiểm thử và có thể xây dựng, trình diễn trong một ngày.

## 3. Scope / Phạm vi

### 3.1 In scope / Trong phạm vi

- `MVP-SDD-01`: Create one support ticket for one equipment item. / Tạo một ticket hỗ trợ cho một thiết bị.
- `MVP-SDD-02`: Store requester, title, description, priority, and equipment information. / Lưu người yêu cầu, tiêu đề, mô tả, mức ưu tiên và thông tin thiết bị.
- `MVP-SDD-03`: Assign one open ticket to one active IT technician. / Phân công một ticket đang mở cho một kỹ thuật viên IT đang hoạt động.
- `MVP-SDD-04`: Start work on an assigned ticket. / Bắt đầu xử lý ticket đã được phân công.
- `MVP-SDD-05`: Resolve an in-progress ticket with a resolution note. / Hoàn tất ticket đang xử lý với ghi chú kết quả.
- `MVP-SDD-06`: Return structured business errors for invalid operations. / Trả về lỗi nghiệp vụ có cấu trúc cho thao tác không hợp lệ.

### 3.2 Out of scope / Ngoài phạm vi

- Email, chat, SMS, or push notifications / Thông báo email, chat, SMS hoặc push
- File attachments and screenshots / Tệp đính kèm và ảnh chụp màn hình
- Automatic SLA timers and escalation / Bộ đếm SLA và tự động chuyển cấp
- Ticket comments and activity feed / Bình luận và luồng hoạt động
- Approval workflows / Quy trình phê duyệt
- Asset inventory and replacement management / Quản lý tài sản và thay thế
- Knowledge base and self-service articles / Knowledge base và bài viết tự phục vụ
- Multiple technicians or teams on one ticket / Nhiều kỹ thuật viên hoặc đội trên một ticket
- User authentication and enterprise permissions / Xác thực và phân quyền doanh nghiệp
- Reporting dashboard and external integrations / Dashboard báo cáo và tích hợp ngoài

## 4. Users and Roles / Người dùng và vai trò

| Role / Vai trò | Main responsibilities / Trách nhiệm chính |
| --- | --- |
| `EMPLOYEE` | Create a ticket and view its status / Tạo ticket và xem trạng thái |
| `TECH_LEAD` | Assign an open ticket to one technician / Phân công ticket cho một kỹ thuật viên |
| `IT_TECHNICIAN` | Start work and resolve an assigned ticket / Bắt đầu xử lý và hoàn tất ticket |
| `ADMIN` | View and manage all demo data / Xem và quản lý dữ liệu demo |

For the one-day MVP, roles may be supplied by a request header or a simple role selector. Real login and permission enforcement are future work.

Trong MVP một ngày, vai trò có thể được truyền bằng request header hoặc bộ chọn vai trò đơn giản. Đăng nhập thật và phân quyền đầy đủ là phần phát triển sau.

## 5. Main Business Flow / Quy trình nghiệp vụ chính

```text
Employee creates ticket
    -> Ticket is OPEN
    -> Tech Lead assigns technician
    -> Ticket is ASSIGNED
    -> Technician starts work
    -> Ticket is IN_PROGRESS
    -> Technician adds resolution note
    -> Ticket is RESOLVED
```

```text
Nhân viên tạo ticket
    -> Ticket ở trạng thái OPEN
    -> Tech Lead phân công kỹ thuật viên
    -> Ticket ở trạng thái ASSIGNED
    -> Kỹ thuật viên bắt đầu xử lý
    -> Ticket ở trạng thái IN_PROGRESS
    -> Kỹ thuật viên thêm ghi chú kết quả
    -> Ticket ở trạng thái RESOLVED
```

## 6. Core Data / Dữ liệu chính

### 6.1 Employee / Nhân viên

- Employee ID / Mã nhân viên
- Employee name / Tên nhân viên
- Department / Phòng ban
- Email
- Role: `EMPLOYEE`, `TECH_LEAD`, `IT_TECHNICIAN`, `ADMIN` / Vai trò
- Status: `ACTIVE`, `INACTIVE` / Trạng thái

### 6.2 Equipment / Thiết bị

- Equipment ID / Mã thiết bị
- Asset code / Mã tài sản
- Equipment name / Tên thiết bị
- Equipment type / Loại thiết bị
- Location / Vị trí
- Owner employee ID / Mã nhân viên sử dụng
- Status: `ACTIVE`, `INACTIVE` / Trạng thái

### 6.3 Support Ticket / Ticket hỗ trợ

- Ticket ID / Mã ticket
- Ticket number / Số ticket
- Requester employee ID / Mã người yêu cầu
- Equipment ID / Mã thiết bị
- Title / Tiêu đề
- Description / Mô tả
- Priority: `LOW`, `MEDIUM`, `HIGH`, `URGENT` / Mức ưu tiên
- Assigned technician ID / Mã kỹ thuật viên được phân công
- Resolution note / Ghi chú kết quả
- Created time / Thời gian tạo
- Updated time / Thời gian cập nhật
- Status: `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`

## 7. Functional Requirements / Yêu cầu chức năng

**Implementation priority:** For this SDD increment, implement only `MVP-SDD-01` to `MVP-SDD-06`. The broader requirements below describe future product capability and must not be added to the one-day implementation without a new work order.

**Ưu tiên triển khai:** Trong increment SDD này, chỉ triển khai `MVP-SDD-01` đến `MVP-SDD-06`. Các yêu cầu mở rộng bên dưới mô tả năng lực tương lai và không được thêm vào bản một ngày nếu chưa có work order mới.

### FR-01: Create ticket / Tạo ticket

**MVP-SDD-01**

The system shall allow an active employee to create a ticket for one active equipment item.

Hệ thống cho phép nhân viên đang hoạt động tạo ticket cho một thiết bị đang hoạt động.

Title, description, requester ID, and equipment ID are required. A new ticket shall start with status `OPEN`.

Tiêu đề, mô tả, mã người yêu cầu và mã thiết bị là bắt buộc. Ticket mới phải bắt đầu ở trạng thái `OPEN`.

### FR-02: Store ticket details / Lưu thông tin ticket

**MVP-SDD-02**

The system shall store title, description, priority, requester, equipment, creation time, and current status.

Hệ thống phải lưu tiêu đề, mô tả, mức ưu tiên, người yêu cầu, thiết bị, thời gian tạo và trạng thái hiện tại.

Priority defaults to `MEDIUM` when the requester does not provide a priority.

Mức ưu tiên mặc định là `MEDIUM` khi người yêu cầu không cung cấp mức ưu tiên.

### FR-03: Assign technician / Phân công kỹ thuật viên

**MVP-SDD-03**

A Tech Lead shall be able to assign one active IT technician to an `OPEN` ticket.

Tech Lead được phép phân công một kỹ thuật viên IT đang hoạt động cho ticket `OPEN`.

A successful assignment changes the ticket status to `ASSIGNED`.

Phân công thành công chuyển ticket sang trạng thái `ASSIGNED`.

### FR-04: Start work / Bắt đầu xử lý

**MVP-SDD-04**

The assigned technician shall be able to start work on the ticket.

Kỹ thuật viên được phân công được phép bắt đầu xử lý ticket.

Starting work changes the ticket status from `ASSIGNED` to `IN_PROGRESS`.

Bắt đầu xử lý chuyển ticket từ `ASSIGNED` sang `IN_PROGRESS`.

### FR-05: Resolve ticket / Hoàn tất ticket

**MVP-SDD-05**

The assigned technician shall be able to resolve an `IN_PROGRESS` ticket by providing a non-empty resolution note.

Kỹ thuật viên được phân công được phép hoàn tất ticket `IN_PROGRESS` bằng cách cung cấp ghi chú kết quả không rỗng.

Resolving the ticket changes its status to `RESOLVED`.

Hoàn tất ticket chuyển trạng thái thành `RESOLVED`.

### FR-06: Structured business errors / Lỗi nghiệp vụ có cấu trúc

**MVP-SDD-06**

The system shall reject invalid transitions, inactive users, unknown equipment, duplicate assignment, and empty resolution notes.

Hệ thống phải từ chối chuyển trạng thái không hợp lệ, người dùng không hoạt động, thiết bị không tồn tại, phân công lặp và ghi chú kết quả rỗng.

Every business error shall contain a stable error code and a human-readable message.

Mỗi lỗi nghiệp vụ phải có mã lỗi ổn định và thông báo dễ hiểu.

## 8. Business Rules / Quy tắc nghiệp vụ

| ID | English rule | Quy tắc tiếng Việt |
| --- | --- | --- |
| BR-01 | Ticket title and description must not be blank. | Tiêu đề và mô tả ticket không được để trống. |
| BR-02 | Requester and equipment must be active. | Người yêu cầu và thiết bị phải đang hoạt động. |
| BR-03 | A new ticket always starts with status `OPEN`. | Ticket mới luôn bắt đầu ở trạng thái `OPEN`. |
| BR-04 | Only a Tech Lead can assign a technician. | Chỉ Tech Lead được phân công kỹ thuật viên. |
| BR-05 | Only an active IT technician can be assigned. | Chỉ kỹ thuật viên IT đang hoạt động mới được phân công. |
| BR-06 | An `OPEN` ticket can be assigned only once in this MVP. | Ticket `OPEN` chỉ được phân công một lần trong MVP. |
| BR-07 | Only the assigned technician can start work. | Chỉ kỹ thuật viên được phân công mới được bắt đầu xử lý. |
| BR-08 | Only an `IN_PROGRESS` ticket can be resolved. | Chỉ ticket `IN_PROGRESS` mới được hoàn tất. |
| BR-09 | A resolution note is required to resolve a ticket. | Phải có ghi chú kết quả để hoàn tất ticket. |
| BR-10 | A `RESOLVED` ticket cannot be changed in this MVP. | Ticket `RESOLVED` không được thay đổi trong MVP. |
| BR-11 | A ticket cannot skip a status. | Ticket không được bỏ qua trạng thái. |

## 9. Status Workflow / Luồng trạng thái

```text
OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED
```

Only the following transitions are valid:

Chỉ các chuyển trạng thái sau là hợp lệ:

| From / Từ | Action / Thao tác | To / Đến | Actor / Người thực hiện |
| --- | --- | --- | --- |
| `OPEN` | Assign technician / Phân công | `ASSIGNED` | `TECH_LEAD` |
| `ASSIGNED` | Start work / Bắt đầu xử lý | `IN_PROGRESS` | Assigned `IT_TECHNICIAN` |
| `IN_PROGRESS` | Resolve with note / Hoàn tất có ghi chú | `RESOLVED` | Assigned `IT_TECHNICIAN` |

Any other status transition shall be rejected.

Mọi chuyển trạng thái khác phải bị từ chối.

## 10. User Interface or API / Giao diện hoặc API

The one-day MVP should provide four operations:

MVP một ngày nên cung cấp bốn thao tác:

1. **Create Ticket**: enter requester, equipment, title, description, and priority.  
   **Tạo ticket**: nhập người yêu cầu, thiết bị, tiêu đề, mô tả và mức ưu tiên.
2. **Assign Ticket**: select one technician for an open ticket.  
   **Phân công ticket**: chọn một kỹ thuật viên cho ticket đang mở.
3. **Start Work**: move an assigned ticket to in-progress.  
   **Bắt đầu xử lý**: chuyển ticket đã phân công sang đang xử lý.
4. **Resolve Ticket**: enter a resolution note and close the ticket.  
   **Hoàn tất ticket**: nhập ghi chú kết quả và đóng ticket.

The interface shall show the current status, assigned technician, and business errors clearly.

Giao diện phải hiển thị rõ trạng thái hiện tại, kỹ thuật viên được phân công và lỗi nghiệp vụ.

## 11. Non-Functional Requirements / Yêu cầu phi chức năng

### NFR-01: Performance / Hiệu năng

Normal ticket operations should complete within two seconds on a local development environment with up to 10,000 tickets.

Các thao tác ticket thông thường nên hoàn thành trong hai giây trên môi trường phát triển với tối đa 10.000 ticket.

### NFR-02: Reliability / Độ tin cậy

A status transition shall be atomic. A failed assignment, start, or resolution shall not leave a partially changed ticket.

Chuyển trạng thái phải có tính nguyên tử. Phân công, bắt đầu hoặc hoàn tất thất bại không được để ticket ở trạng thái thay đổi dở dang.

### NFR-03: Security and privacy / Bảo mật và riêng tư

The MVP shall not log full descriptions, employee contact details, or unnecessary ticket data. Role checks shall be represented in the application design even if real authentication is deferred.

MVP không được ghi log đầy đủ mô tả, thông tin liên hệ nhân viên hoặc dữ liệu ticket không cần thiết. Kiểm tra vai trò phải xuất hiện trong thiết kế ứng dụng dù xác thực thật được để sau.

### NFR-04: Maintainability / Khả năng bảo trì

Ticket state rules shall be implemented in the domain model or application service, not duplicated in controllers and templates.

Quy tắc trạng thái ticket phải được triển khai trong domain model hoặc application service, không lặp lại ở controller và template.

### NFR-05: Usability / Khả năng sử dụng

The main ticket lifecycle shall be understandable from the current status and action response without requiring technical knowledge from the employee.

Nhân viên có thể hiểu vòng đời ticket qua trạng thái hiện tại và phản hồi thao tác mà không cần kiến thức kỹ thuật.

## 12. API or Service Operations / API hoặc thao tác service

The implementation may use MVC controllers or REST APIs. At minimum, it shall support operations equivalent to:

Có thể triển khai bằng MVC controller hoặc REST API. Tối thiểu phải có các thao tác tương đương:

- `POST /api/tickets` - create an open ticket / tạo ticket mở
- `POST /api/tickets/{id}/assign` - assign one technician / phân công một kỹ thuật viên
- `POST /api/tickets/{id}/start` - start work / bắt đầu xử lý
- `POST /api/tickets/{id}/resolve` - resolve with a note / hoàn tất với ghi chú
- `GET /api/tickets/{id}` - view ticket status / xem trạng thái ticket

## 13. Acceptance Criteria / Tiêu chí nghiệm thu

The MVP is accepted when all of the following are true:

MVP được nghiệm thu khi tất cả điều kiện sau đúng:

1. An active employee can create a ticket for an active equipment item.  
   Nhân viên đang hoạt động có thể tạo ticket cho thiết bị đang hoạt động.
2. A new ticket has status `OPEN` and default priority `MEDIUM`.  
   Ticket mới có trạng thái `OPEN` và mức ưu tiên mặc định `MEDIUM`.
3. A Tech Lead can assign one active technician to an open ticket.  
   Tech Lead có thể phân công một kỹ thuật viên đang hoạt động cho ticket mở.
4. Assignment changes the ticket status to `ASSIGNED`.  
   Phân công chuyển ticket sang `ASSIGNED`.
5. Only the assigned technician can start work.  
   Chỉ kỹ thuật viên được phân công mới có thể bắt đầu xử lý.
6. Starting work changes the status to `IN_PROGRESS`.  
   Bắt đầu xử lý chuyển trạng thái sang `IN_PROGRESS`.
7. The assigned technician can resolve the ticket with a non-empty note.  
   Kỹ thuật viên được phân công có thể hoàn tất ticket với ghi chú không rỗng.
8. Resolving changes the status to `RESOLVED`.  
   Hoàn tất chuyển trạng thái sang `RESOLVED`.
9. Invalid transitions return a stable business error and do not change the ticket.  
   Chuyển trạng thái không hợp lệ trả về lỗi ổn định và không thay đổi ticket.
10. The complete lifecycle can be demonstrated within one day of development.  
    Vòng đời hoàn chỉnh có thể được trình diễn sau một ngày phát triển.

## 14. Test Scenarios / Kịch bản kiểm thử

| Scenario / Kịch bản | Expected result / Kết quả mong đợi |
| --- | --- |
| Create ticket with blank title / Tạo ticket không có tiêu đề | Reject with `INVALID_TICKET` / Từ chối với `INVALID_TICKET` |
| Create ticket for inactive equipment / Tạo ticket cho thiết bị không hoạt động | Reject with `EQUIPMENT_NOT_ACTIVE` / Từ chối với `EQUIPMENT_NOT_ACTIVE` |
| Assign open ticket / Phân công ticket mở | Status becomes `ASSIGNED` / Chuyển sang `ASSIGNED` |
| Assign already assigned ticket / Phân công ticket đã phân công | Reject with `TICKET_NOT_ASSIGNABLE` / Từ chối với `TICKET_NOT_ASSIGNABLE` |
| Start work as another technician / Kỹ thuật viên khác bắt đầu xử lý | Reject with `NOT_ASSIGNED_TECHNICIAN` / Từ chối với `NOT_ASSIGNED_TECHNICIAN` |
| Start work on open ticket / Bắt đầu xử lý ticket mở | Reject with `INVALID_STATUS_TRANSITION` / Từ chối với `INVALID_STATUS_TRANSITION` |
| Resolve without note / Hoàn tất không có ghi chú | Reject with `RESOLUTION_NOTE_REQUIRED` / Từ chối với `RESOLUTION_NOTE_REQUIRED` |
| Resolve in-progress ticket / Hoàn tất ticket đang xử lý | Status becomes `RESOLVED` / Chuyển sang `RESOLVED` |
| Change resolved ticket / Thay đổi ticket đã hoàn tất | Reject with `TICKET_NOT_EDITABLE` / Từ chối với `TICKET_NOT_EDITABLE` |

## 15. One-Day Delivery Plan / Kế hoạch triển khai trong một ngày

| Time / Thời gian | Work / Công việc |
| --- | --- |
| 1 hour / 1 giờ | Confirm ticket states, roles, and domain rules / Chốt trạng thái, vai trò và quy tắc domain |
| 2 hours / 2 giờ | Implement ticket, employee, and equipment domain objects / Tạo domain ticket, nhân viên và thiết bị |
| 2 hours / 2 giờ | Implement assignment, start, and resolve service / Tạo service phân công, bắt đầu và hoàn tất |
| 2 hours / 2 giờ | Implement API/controller and error mapping / Tạo API/controller và ánh xạ lỗi |
| 1 hour / 1 giờ | Add domain and API tests / Viết test domain và API |
| 1 hour / 1 giờ | Run demo, fix defects, and document / Demo, sửa lỗi và hoàn thiện tài liệu |

## 16. Assumptions and Open Decisions / Giả định và quyết định mở

- The MVP uses one IT support team. / MVP chỉ dùng một đội IT.
- Each ticket has one requester and one assigned technician. / Mỗi ticket có một người yêu cầu và một kỹ thuật viên được phân công.
- A ticket concerns one equipment item. / Một ticket liên quan đến một thiết bị.
- Resolution means the technician reports the technical work as complete; employee confirmation is future work. / Hoàn tất nghĩa là kỹ thuật viên báo đã xử lý xong; xác nhận của nhân viên là phần sau.
- Real authentication and authorization are future work. / Xác thực và phân quyền thật là phần sau.
- The product owner must confirm whether `RESOLVED` should later be followed by `CLOSED`. / Chủ sản phẩm phải xác nhận sau này có cần trạng thái `CLOSED` sau `RESOLVED` hay không.
- The product owner must confirm whether priority changes are allowed after ticket creation. / Chủ sản phẩm phải xác nhận có cho đổi mức ưu tiên sau khi tạo ticket hay không.

## 17. Glossary / Thuật ngữ

| Term / Thuật ngữ | Meaning / Ý nghĩa |
| --- | --- |
| Ticket | A recorded request for IT support / Yêu cầu hỗ trợ IT được ghi nhận |
| Requester | Employee who reports the problem / Nhân viên báo cáo sự cố |
| Tech Lead | Person who assigns technical work / Người phân công công việc kỹ thuật |
| IT technician | Person who investigates and resolves the problem / Người kiểm tra và xử lý sự cố |
| Equipment | Computer or device related to the ticket / Máy tính hoặc thiết bị liên quan đến ticket |
| Resolution note | Short explanation of the work completed / Mô tả ngắn công việc đã hoàn tất |
| Status transition | Change from one ticket status to another / Chuyển từ trạng thái này sang trạng thái khác |
