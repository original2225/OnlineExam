package com.example.controller;

import com.example.common.Result;
import com.example.entity.Poll;
import com.example.entity.PollOption;
import com.example.entity.PollVote;
import com.example.mapper.PollMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/poll")
public class PollController {

    @Resource
    private PollMapper pollMapper;

    @PostMapping("/add")
    @Transactional
    public Result add(@RequestBody Map<String, Object> params) {
        Integer creatorId = ((Number) params.get("creatorId")).intValue();
        String creatorName = (String) params.get("creatorName");
        String creatorRole = (String) params.get("creatorRole");

        Poll poll = new Poll();
        poll.setTitle((String) params.get("title"));
        poll.setDescription((String) params.get("description"));
        poll.setCoverUrl((String) params.get("coverUrl"));
        poll.setCreatorId(creatorId);
        poll.setCreatorName(creatorName);
        poll.setCreatorRole(creatorRole);
        poll.setMaxChoices(params.get("maxChoices") != null ? ((Number) params.get("maxChoices")).intValue() : 1);
        poll.setIsAnonymous(params.get("isAnonymous") != null && Boolean.parseBoolean(params.get("isAnonymous").toString()));

        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            poll.setStartTime(LocalDateTime.parse(startTimeStr));
        }
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            poll.setEndTime(LocalDateTime.parse(endTimeStr));
        }

        poll.setStatus("active");
        poll.setTotalVotes(0);
        pollMapper.insert(poll);

        List<Map<String, Object>> optionsData = (List<Map<String, Object>>) params.get("options");
        if (optionsData != null) {
            String[] defaultColors = {"#00b42a", "#409eff", "#ff7d00", "#722ed1", "#f53f3f", "#0fc6c2", "#f77234", "#3491fa"};
            for (int i = 0; i < optionsData.size(); i++) {
                Map<String, Object> opt = optionsData.get(i);
                PollOption option = new PollOption();
                option.setPollId(poll.getId());
                option.setContent((String) opt.get("content"));
                option.setColor(opt.get("color") != null ? (String) opt.get("color") : defaultColors[i % defaultColors.length]);
                option.setVoteCount(0);
                option.setOrderNum(i);
                pollMapper.insertOption(option);
            }
        }

        return Result.success(poll);
    }

    @PutMapping("/update")
    @Transactional
    public Result update(@RequestBody Map<String, Object> params) {
        Integer id = ((Number) params.get("id")).intValue();
        Poll poll = pollMapper.selectById(id);
        if (poll == null) return Result.error("投票不存在");

        poll.setTitle((String) params.get("title"));
        poll.setDescription((String) params.get("description"));
        poll.setCoverUrl((String) params.get("coverUrl"));
        poll.setMaxChoices(params.get("maxChoices") != null ? ((Number) params.get("maxChoices")).intValue() : 1);
        poll.setIsAnonymous(params.get("isAnonymous") != null && Boolean.parseBoolean(params.get("isAnonymous").toString()));

        String endTimeStr = (String) params.get("endTime");
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            poll.setEndTime(LocalDateTime.parse(endTimeStr));
        }

        pollMapper.updateById(poll);

        if (params.containsKey("options")) {
            pollMapper.deleteOptionsByPollId(id);
            List<Map<String, Object>> optionsData = (List<Map<String, Object>>) params.get("options");
            if (optionsData != null) {
                String[] defaultColors = {"#00b42a", "#409eff", "#ff7d00", "#722ed1", "#f53f3f", "#0fc6c2", "#f77234", "#3491fa"};
                for (int i = 0; i < optionsData.size(); i++) {
                    Map<String, Object> opt = optionsData.get(i);
                    PollOption option = new PollOption();
                    option.setPollId(id);
                    option.setContent((String) opt.get("content"));
                    option.setColor(opt.get("color") != null ? (String) opt.get("color") : defaultColors[i % defaultColors.length]);
                    option.setVoteCount(0);
                    option.setOrderNum(i);
                    pollMapper.insertOption(option);
                }
            }
        }

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        pollMapper.deleteById(id);
        pollMapper.deleteOptionsByPollId(id);
        return Result.success();
    }

    @PutMapping("/end/{id}")
    public Result endPoll(@PathVariable Integer id) {
        Poll poll = pollMapper.selectById(id);
        if (poll == null) return Result.error("投票不存在");
        poll.setStatus("ended");
        poll.setEndTime(LocalDateTime.now());
        pollMapper.updateById(poll);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id, @RequestParam(required = false) Integer userId) {
        Poll poll = pollMapper.selectById(id);
        if (poll == null) return Result.error("投票不存在");

        List<PollOption> options = pollMapper.selectOptionsByPollId(id);
        enrichPollData(poll, options, userId);
        poll.setOptions(options);
        return Result.success(poll);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer userId) {
        PageHelper.startPage(pageNum, pageSize);
        Poll query = new Poll();
        if (status != null) query.setStatus(status);
        List<Poll> list = pollMapper.selectAll(query);
        PageInfo<Poll> pageInfo = new PageInfo<>(list);

        for (Poll poll : pageInfo.getList()) {
            List<PollOption> options = pollMapper.selectOptionsByPollId(poll.getId());
            enrichPollData(poll, options, userId);
            poll.setOptions(options);
        }

        return Result.success(pageInfo);
    }

    @GetMapping("/activePolls")
    public Result activePolls(@RequestParam(required = false) Integer userId) {
        List<Poll> list = pollMapper.selectActivePolls();
        for (Poll poll : list) {
            List<PollOption> options = pollMapper.selectOptionsByPollId(poll.getId());
            enrichPollData(poll, options, userId);
            poll.setOptions(options);
        }
        return Result.success(list);
    }

    @PostMapping("/vote")
    @Transactional
    public Result vote(@RequestBody Map<String, Object> params) {
        Integer pollId = ((Number) params.get("pollId")).intValue();
        Integer userId = ((Number) params.get("userId")).intValue();
        String userRole = (String) params.get("userRole");
        List<Number> optionIds = (List<Number>) params.get("optionIds");

        if (optionIds == null || optionIds.isEmpty()) {
            return Result.error("请选择至少一个选项");
        }

        Poll poll = pollMapper.selectById(pollId);
        if (poll == null) return Result.error("投票不存在");
        if ("ended".equals(poll.getStatus())) return Result.error("投票已结束");
        if (poll.getEndTime() != null && LocalDateTime.now().isAfter(poll.getEndTime())) {
            return Result.error("投票已截止");
        }
        if (poll.getMaxChoices() != null && optionIds.size() > poll.getMaxChoices()) {
            return Result.error("最多只能选择" + poll.getMaxChoices() + "个选项");
        }

        int existingVotes = pollMapper.hasVoted(pollId, userId);
        if (existingVotes > 0) {
            return Result.error("你已经投过票了");
        }

        for (Number optId : optionIds) {
            int optionId = optId.intValue();
            PollVote vote = new PollVote();
            vote.setPollId(pollId);
            vote.setOptionId(optionId);
            vote.setUserId(userId);
            vote.setUserRole(userRole);
            pollMapper.insertVote(vote);
            pollMapper.incrementOptionVote(optionId);
        }
        pollMapper.incrementPollTotalVotes(pollId);

        Poll updated = pollMapper.selectById(pollId);
        List<PollOption> options = pollMapper.selectOptionsByPollId(pollId);
        enrichPollData(updated, options, userId);
        updated.setOptions(options);
        updated.setHasVoted(true);
        updated.setVotedOptionIds(optionIds.stream().map(Number::intValue).collect(java.util.stream.Collectors.toList()));

        return Result.success(updated);
    }

    private void enrichPollData(Poll poll, List<PollOption> options, Integer userId) {
        int total = 0;
        for (PollOption opt : options) {
            total += (opt.getVoteCount() != null ? opt.getVoteCount() : 0);
        }
        poll.setTotalVotes(total);

        for (PollOption opt : options) {
            if (total > 0 && opt.getVoteCount() != null) {
                opt.setPercentage(Math.round(opt.getVoteCount() * 10000.0 / total) / 100.0);
            } else {
                opt.setPercentage(0.0);
            }
        }

        if (userId != null) {
            int hasVoted = pollMapper.hasVoted(poll.getId(), userId);
            poll.setHasVoted(hasVoted > 0);
            if (hasVoted > 0) {
                poll.setVotedOptionIds(pollMapper.selectVotedOptionIds(poll.getId(), userId));
            }
        } else {
            poll.setHasVoted(false);
        }
    }
}
