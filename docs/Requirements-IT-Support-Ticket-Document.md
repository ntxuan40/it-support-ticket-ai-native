Yêu cầu nghiệp vụ	
Hệ thống cung cấp quy trình quản lý yêu cầu hỗ trợ IT nội bộ như sau	
1	Nhân viên tạo yêu cầu hỗ trợ cho các sự cố như máy in, máy tính hoặc máy ảo. Yêu cầu phải bao gồm thiết bị, tiêu đề và mô tả sự cố. Nhân viên có thể tra cứu trạng thái và người đang xử lý yêu cầu.
2	Quản lý IT tiếp nhận các yêu cầu mới, kiểm tra nội dung và phân công cho một nhân viên IT phù hợp. Mỗi yêu cầu chỉ có một nhân viên IT chịu trách nhiệm tại một thời điểm.
3	Nhân viên IT xem các yêu cầu được phân công, xác nhận bắt đầu xử lý, cập nhật trạng thái và ghi nhận kết quả xử lý hoặc hướng dẫn khắc phục. Trạng thái yêu cầu được cập nhật theo quy trình: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED.
4	Quản trị viên quản lý người dùng, vai trò, thiết bị và dữ liệu hệ thống; đồng thời có quyền tra cứu toàn bộ yêu cầu để hỗ trợ vận hành và kiểm tra hệ thống.

## Requirement Decomposition

### BR-001: Tạo yêu cầu hỗ trợ IT bởi nhân viên
- Actor: Nhân viên
- Business capability: Tạo và theo dõi yêu cầu hỗ trợ IT
- Atomic functional requirements:
  - FR-001: Hệ thống cho phép nhân viên tạo một yêu cầu hỗ trợ cho sự cố liên quan đến máy in, máy tính hoặc máy ảo.
  - FR-002: Hệ thống yêu cầu thông tin thiết bị khi tạo yêu cầu.
  - FR-003: Hệ thống yêu cầu tiêu đề của yêu cầu khi tạo yêu cầu.
  - FR-004: Hệ thống yêu cầu mô tả sự cố khi tạo yêu cầu.
  - FR-005: Hệ thống cho phép nhân viên tra cứu trạng thái hiện tại của yêu cầu.
  - FR-006: Hệ thống cho phép nhân viên tra cứu người đang xử lý yêu cầu.
- Explicitly stated business rules:
  - BRULE-001: Mỗi yêu cầu phải bao gồm thiết bị, tiêu đề và mô tả sự cố.
  - BRULE-002: Nhân viên có thể tra cứu trạng thái và người đang xử lý yêu cầu.
- Acceptance criteria:
  - AC-001: Given nhân viên đã đăng nhập vào hệ thống, When nhân viên tạo một yêu cầu hỗ trợ cho sự cố máy in, Then hệ thống ghi nhận yêu cầu với thông tin thiết bị, tiêu đề và mô tả sự cố.
  - AC-002: Given yêu cầu đã được tạo, When nhân viên xem chi tiết yêu cầu, Then hệ thống hiển thị trạng thái hiện tại và người đang xử lý yêu cầu.
- Missing information:
  - Không có thông tin về cách nhân viên xác định hoặc chọn thiết bị trong hệ thống.
  - Không có thông tin về loại thông tin mô tả sự cố có bắt buộc hay có thể đính kèm tệp tin hay không.
  - Không có thông tin về số lượng yêu cầu tối đa mà một nhân viên có thể tạo.
- Ambiguity:
  - "Thiết bị" có thể là tên thiết bị cụ thể, mã tài sản, hoặc danh mục thiết bị được quản lý bởi hệ thống hay không.
  - "Người đang xử lý yêu cầu" chưa xác định rõ là người được phân công, người đang làm việc, hay cả hai.
  - "Tra cứu trạng thái" không nêu rõ xem người dùng có thể xem tất cả yêu cầu của mình hay toàn bộ yêu cầu trong hệ thống hay không.
- Assumptions that must not be treated as confirmed requirements:
  - ASM-001: Có thể tồn tại danh sách người dùng/thiết bị đã được quản lý trước khi tạo yêu cầu.
  - ASM-002: Nhân viên chỉ có thể xem yêu cầu của chính mình.
- Open questions for the business owner:
  - AQ-001: Người dùng có được phép tạo yêu cầu cho thiết bị không thuộc quyền quản lý của mình hay không?
  - AQ-002: Thông tin trạng thái và người xử lý cần hiển thị cho nhân viên trong phạm vi yêu cầu nào?

### BR-002: Tiếp nhận và phân công yêu cầu bởi quản lý IT
- Actor: Quản lý IT
- Business capability: Tiếp nhận, kiểm tra và phân công yêu cầu hỗ trợ
- Atomic functional requirements:
  - FR-007: Hệ thống cho phép quản lý IT nhìn thấy các yêu cầu mới.
  - FR-008: Hệ thống cho phép quản lý IT kiểm tra nội dung của mỗi yêu cầu mới.
  - FR-009: Hệ thống cho phép quản lý IT phân công yêu cầu cho một nhân viên IT phù hợp.
  - FR-010: Hệ thống cho phép mỗi yêu cầu được gán tối đa một nhân viên IT chịu trách nhiệm tại một thời điểm.
- Explicitly stated business rules:
  - BRULE-003: Quản lý IT tiếp nhận các yêu cầu mới.
  - BRULE-004: Quản lý IT kiểm tra nội dung của yêu cầu trước khi phân công.
  - BRULE-005: Mỗi yêu cầu chỉ có một nhân viên IT chịu trách nhiệm tại một thời điểm.
- Acceptance criteria:
  - AC-003: Given một yêu cầu mới đang chờ xử lý, When quản lý IT mở danh sách yêu cầu mới, Then hệ thống hiển thị yêu cầu đó để quản lý IT xem xét.
  - AC-004: Given quản lý IT đã xác nhận nội dung yêu cầu, When quản lý IT phân công cho một nhân viên IT phù hợp, Then hệ thống gán một và chỉ một nhân viên IT chịu trách nhiệm cho yêu cầu đó.
- Missing information:
  - Không có thông tin về cách hệ thống xác định "nhân viên IT phù hợp".
  - Không có thông tin về quy trình tiếp nhận yêu cầu mới (tự động, thủ công, theo hàng đợi, hoặc theo vai trò).
  - Không có thông tin về việc quản lý IT có thể từ chối, yêu cầu bổ sung thông tin, hoặc trả lại yêu cầu cho nhân viên không hay không.
- Ambiguity:
  - "Kiểm tra nội dung" chưa xác định rõ là xác thực thông tin, bổ sung thông tin, hay chỉ xem xét ban đầu.
  - "Phù hợp" chưa được định nghĩa bằng kỹ năng, nhóm, hoặc mức độ ưu tiên.
- Assumptions that must not be treated as confirmed requirements:
  - ASM-003: Phân công được thực hiện theo cách thủ công bởi quản lý IT.
  - ASM-004: Mỗi yêu cầu chỉ có thể được gán cho một người khi đang active.
- Open questions for the business owner:
  - AQ-003: Có cần phân loại yêu cầu theo nhóm kỹ thuật hoặc mức độ ưu tiên để hỗ trợ việc phân công hay không?
  - AQ-004: Quản lý IT có thể chuyển giao yêu cầu giữa các nhân viên IT trong quá trình xử lý hay không?

### BR-003: Xử lý yêu cầu và cập nhật trạng thái bởi nhân viên IT
- Actor: Nhân viên IT
- Business capability: Xem, nhận xử lý, cập nhật tiến độ và ghi nhận kết quả xử lý
- Atomic functional requirements:
  - FR-011: Hệ thống cho phép nhân viên IT xem các yêu cầu được phân công cho mình.
  - FR-012: Hệ thống cho phép nhân viên IT xác nhận bắt đầu xử lý yêu cầu.
  - FR-013: Hệ thống cho phép nhân viên IT cập nhật trạng thái của yêu cầu.
  - FR-014: Hệ thống cho phép nhân viên IT ghi nhận kết quả xử lý hoặc hướng dẫn khắc phục.
  - FR-015: Hệ thống phải hiển thị chu kỳ trạng thái yêu cầu theo trình tự: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED.
- Explicitly stated business rules:
  - BRULE-006: Nhân viên IT xem các yêu cầu được phân công.
  - BRULE-007: Nhân viên IT xác nhận bắt đầu xử lý.
  - BRULE-008: Trạng thái yêu cầu được cập nhật theo quy trình: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED.
- Acceptance criteria:
  - AC-005: Given nhân viên IT đã được phân công cho một yêu cầu, When nhân viên IT mở danh sách yêu cầu được phân công, Then hệ thống hiển thị yêu cầu đó trong danh sách của nhân viên IT.
  - AC-006: Given yêu cầu đã ở trạng thái ASSIGNED, When nhân viên IT xác nhận bắt đầu xử lý, Then trạng thái của yêu cầu được cập nhật thành IN_PROGRESS.
  - AC-007: Given yêu cầu đang được xử lý, When nhân viên IT ghi nhận kết quả xử lý hoặc hướng dẫn khắc phục, Then hệ thống lưu thông tin kết quả hoặc hướng dẫn khắc phục và cập nhật trạng thái thành RESOLVED.
- Missing information:
  - Không có thông tin về trạng thái mở ban đầu OPEN được tạo ra như thế nào và bởi ai.
  - Không có thông tin về việc có cần ghi nhận thời gian bắt đầu, thời gian hoàn thành, hoặc các bản ghi lịch sử không hay không.
  - Không có thông tin về việc hướng dẫn khắc phục có được lưu dưới dạng văn bản, đánh dấu, hoặc tệp đính kèm hay không.
- Ambiguity:
  - Quy trình trạng thái không nói rõ xem nhân viên IT có được phép cập nhật trực tiếp từ OPEN sang IN_PROGRESS mà không cần ASSIGNED hay không.
  - "Ghi nhận kết quả xử lý hoặc hướng dẫn khắc phục" chưa xác định hình thức dữ liệu và thời điểm bắt buộc.
- Assumptions that must not be treated as confirmed requirements:
  - ASM-005: Quy trình trạng thái phải được hệ thống ép buộc theo thứ tự cố định.
  - ASM-006: Chỉ có nhân viên IT được phân công mới có quyền cập nhật trạng thái của yêu cầu.
- Open questions for the business owner:
  - AQ-005: Có cần hỗ trợ trạng thái bổ sung như REJECTED, CLOSED, PENDING, hoặc CANCELLED hay không?
  - AQ-006: Khi yêu cầu được giải quyết, liệu hệ thống có cần tự động gắn nhãn hoặc thông báo cho nhân viên tạo yêu cầu không?

### BR-004: Quản trị viên quản lý dữ liệu và tra cứu toàn bộ yêu cầu
- Actor: Quản trị viên
- Business capability: Quản lý người dùng, vai trò, thiết bị và dữ liệu hệ thống; tra cứu toàn bộ yêu cầu
- Atomic functional requirements:
  - FR-016: Hệ thống cho phép quản trị viên quản lý người dùng.
  - FR-017: Hệ thống cho phép quản trị viên quản lý vai trò.
  - FR-018: Hệ thống cho phép quản trị viên quản lý thiết bị.
  - FR-019: Hệ thống cho phép quản trị viên quản lý dữ liệu hệ thống.
  - FR-020: Hệ thống cho phép quản trị viên tra cứu toàn bộ yêu cầu để hỗ trợ vận hành và kiểm tra hệ thống.
- Explicitly stated business rules:
  - BRULE-009: Quản trị viên quản lý người dùng, vai trò, thiết bị và dữ liệu hệ thống.
  - BRULE-010: Quản trị viên có quyền tra cứu toàn bộ yêu cầu.
- Acceptance criteria:
  - AC-008: Given quản trị viên đã đăng nhập vào hệ thống, When quản trị viên thực hiện thao tác quản lý dữ liệu người dùng, vai trò, thiết bị hoặc dữ liệu hệ thống, Then hệ thống cho phép thao tác quản lý tương ứng.
  - AC-009: Given quản trị viên cần hỗ trợ vận hành hoặc kiểm tra hệ thống, When quản trị viên thực hiện tra cứu toàn bộ yêu cầu, Then hệ thống trả về kết quả theo tiêu chí tra cứu đã được yêu cầu.
- Missing information:
  - Không có thông tin về phạm vi chính xác của "quản lý người dùng, vai trò, thiết bị và dữ liệu hệ thống".
  - Không có thông tin về các tiêu chí tra cứu toàn bộ yêu cầu do quản trị viên thực hiện.
  - Không có thông tin về quyền hạn bổ sung của quản trị viên như tạo, sửa, xóa, hoặc khóa dữ liệu.
- Ambiguity:
  - "Dữ liệu hệ thống" chưa được định nghĩa rõ, có thể bao gồm cấu hình, danh sách thiết bị, danh mục vai trò, hoặc dữ liệu vận hành.
  - "Tra cứu toàn bộ yêu cầu" chưa nêu rõ có cần tra cứu theo người dùng, thiết bị, trạng thái, thời gian, hoặc người xử lý hay không.
- Assumptions that must not be treated as confirmed requirements:
  - ASM-007: Quản trị viên là vai trò đặc biệt trong hệ thống và có quyền cao hơn nhân viên và quản lý IT.
  - ASM-008: Quản trị viên chỉ cần quyền xem và không cần quyền chỉnh sửa dữ liệu hệ thống.
- Open questions for the business owner:
  - AQ-007: Quản trị viên có được phép tạo, chỉnh sửa và xóa dữ liệu hệ thống hay chỉ xem và tra cứu?
  - AQ-008: Cần hỗ trợ tìm kiếm toàn bộ yêu cầu theo các tiêu chí nào để thực hiện vận hành và kiểm tra hệ thống?

### Consolidated traceability summary
- Business Requirements: BR-001 đến BR-004
- Functional Requirements: FR-001 đến FR-020
- Business Rules: BRULE-001 đến BRULE-010
- Acceptance Criteria: AC-001 đến AC-009
- Assumptions: ASM-001 đến ASM-008
- Open Questions: AQ-001 đến AQ-008

> Note: The decomposition above is limited to requirements explicitly present in the source document. Any item marked as assumption or open question is not accepted as a confirmed requirement unless the business owner validates it.
