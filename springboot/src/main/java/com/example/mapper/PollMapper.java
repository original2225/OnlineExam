package com.example.mapper;

import com.example.entity.Poll;
import com.example.entity.PollOption;
import com.example.entity.PollVote;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PollMapper {

    void insert(Poll poll);

    void updateById(Poll poll);

    void deleteById(Integer id);

    @Select("select * from poll where id = #{id}")
    Poll selectById(Integer id);

    List<Poll> selectAll(Poll poll);

    List<Poll> selectActivePolls();

    @Select("select * from poll_option where poll_id = #{pollId} order by order_num, id")
    List<PollOption> selectOptionsByPollId(Integer pollId);

    void insertOption(PollOption option);

    @Delete("delete from poll_option where poll_id = #{pollId}")
    void deleteOptionsByPollId(Integer pollId);

    void insertVote(PollVote vote);

    @Select("select count(*) from poll_vote where poll_id = #{pollId} and user_id = #{userId}")
    int hasVoted(@Param("pollId") Integer pollId, @Param("userId") Integer userId);

    @Select("select option_id from poll_vote where poll_id = #{pollId} and user_id = #{userId}")
    List<Integer> selectVotedOptionIds(@Param("pollId") Integer pollId, @Param("userId") Integer userId);

    @Update("update poll_option set vote_count = vote_count + 1 where id = #{optionId}")
    void incrementOptionVote(Integer optionId);

    @Update("update poll set total_votes = total_votes + 1 where id = #{pollId}")
    void incrementPollTotalVotes(Integer pollId);

    @Select("select count(DISTINCT user_id) from poll_vote where poll_id = #{pollId}")
    int countDistinctVoters(Integer pollId);
}
